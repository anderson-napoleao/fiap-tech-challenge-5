# Sistema Condominio - Tech Challenge

Plataforma distribuida para gestao condominial, estruturada em microservicos com Clean Architecture, mensageria orientada a eventos e observabilidade centralizada.

Este documento explica o desenho da solucao, as decisoes de engenharia, a operacao via Docker, os testes e um roteiro de demonstracao ponta a ponta.

## 1. Objetivo do Projeto

Este sistema foi concebido para resolver um problema operacional real de condominio:

- controlar cadastro de usuarios (moradores e funcionarios);
- registrar recebimento e retirada de encomendas;
- gerar notificacoes para o morador de forma assincrona;
- manter rastreabilidade da operacao com logs estruturados.

Do ponto de vista de engenharia, o foco do projeto e demonstrar:

- separacao de responsabilidades por dominio;
- evolucao segura com arquitetura limpa;
- consistencia entre banco e evento com Outbox + CDC;
- observabilidade realista para ambiente distribuido;
- estrategia de testes em camadas.

## 2. Arquitetura Geral

Arquitetura em microservicos, com responsabilidades bem delimitadas:

- `servico-identidade`: autenticacao e emissao de JWT.
- `servico-usuario`: cadastro e manutencao de perfil.
- `servico-encomenda`: ciclo operacional de encomenda (recebimento e retirada).
- `servico-notificacao`: processamento de eventos e notificacao ao morador.
- `frontend`: interface web para os fluxos funcionais.

Elementos de suporte:

- PostgreSQL separado por servico (isolamento de contexto e de schema).
- Kafka + Kafka Connect + Debezium para CDC de eventos de outbox.
- ELK + Filebeat para centralizacao e analise de logs.

Em termos praticos: cada servico cuida do seu proprio estado, e a integracao entre contextos e feita por API e eventos, evitando acoplamento direto de banco.

### 2.1 Decisoes arquiteturais (e o motivo)

1. Separacao em microservicos por contexto de negocio
- `servico-identidade`, `servico-usuario`, `servico-encomenda` e `servico-notificacao` evoluem com baixo acoplamento.
- Cada contexto tem API e banco proprios, reduzindo impacto de mudancas locais em outros modulos.

2. Clean Architecture aplicada dentro de cada servico
- `domain`: regras de negocio puras, sem framework.
- `application`: casos de uso e portas (entrada/saida), sem Spring.
- `adapter`/`infrastructure`: detalhes tecnicos (HTTP, JPA, Kafka, seguranca, serializacao).
- Regras arquiteturais sao protegidas por testes ArchUnit em cada modulo.

3. Integracao entre contextos por API e eventos
- Chamadas HTTP cobrem fluxos sincronos (ex.: autenticacao e consulta de perfil).
- Eventos cobrem fluxos assincronos e desacoplam tempo de resposta entre servicos.

4. Consistencia entre estado e evento com Outbox + CDC
- Nos servicos que publicam eventos, estado de negocio e `outbox_event` sao persistidos na mesma transacao local.
- Debezium le o `outbox_event` no Postgres e publica no Kafka sem dual-write na aplicacao.

5. Observabilidade como parte da arquitetura
- Logs estruturados, tracing e stack ELK permitem diagnostico distribuido sem depender de logs locais isolados.

### 2.2 Versionamento de banco com Flyway

Padrao adotado no projeto:

- Flyway habilitado em todos os servicos (`spring.flyway.enabled=true`).
- Scripts versionados em `src/main/resources/db/migration`.
- Convencao de nomes: `V{numero}__{descricao}.sql`.
- Evolucao de schema controlada por migracoes; o JPA nao e usado para gerar schema em runtime.

Inventario atual de migracoes:

| Servico | Migracoes |
| --- | --- |
| `servico-identidade` | `V1__init_schema.sql` |
| `servico-usuario` | `V1__init_schema.sql` |
| `servico-encomenda` | `V1__init_schema.sql`, `V2__add_idx_encomendas_bloco_apto_data.sql` |
| `servico-notificacao` | `V1__init_schema.sql` |

Relacao com o modelo de integracao:

- `servico-encomenda` e `servico-notificacao` criam tabelas de negocio e `outbox_event` via migracao.
- Isso garante que o contrato de persistencia exigido pelo pipeline Debezium/Kafka esteja versionado junto com o codigo.

Como evoluir o banco com seguranca:

1. Criar um novo arquivo `V{proxima_versao}__{descricao}.sql` no servico dono do schema.
2. Nunca editar migracoes ja aplicadas em ambiente compartilhado; sempre criar nova versao.
3. Subir o servico (local, Docker ou testes) para Flyway aplicar a nova versao automaticamente.
4. Manter entidades JPA aderentes ao schema versionado (sem depender de auto-DDL).

## 3. Servicos da Aplicacao (Detalhamento)

### 3.1 servico-identidade (porta 8081)

Responsabilidade central:

- autenticar credenciais;
- emitir token JWT;
- administrar identidades e papeis.

Por que existe:

- desacoplar autenticacao da regra de negocio dos demais servicos;
- centralizar politica de acesso e identidade.

Principais endpoints:

- `POST /auth/token`
- `POST /admin/users`
- `PATCH /admin/users/{id}/disable`
- `DELETE /admin/users/{id}`

Persistencia:

- banco `condominio_identidade` (container `postgres-identidade`).

---

### 3.2 servico-usuario (porta 8082)

Responsabilidade central:

- cadastrar usuario/morador;
- expor e atualizar perfil autenticado.

Por que existe:

- concentrar o contexto de dados cadastrais;
- permitir evolucao independente de regra de perfil sem impactar autenticacao.

Principais endpoints:

- `POST /users`
- `GET /users/me`
- `PUT /users/me`

Persistencia:

- banco `condominio_usuario` (container `postgres-usuario`).

Dependencia funcional:

- integra com `servico-identidade` para fluxo de identidade/credencial.

---

### 3.3 servico-encomenda (porta 8083)

Responsabilidade central:

- registrar recebimento de encomenda;
- registrar retirada;
- gerar evento de dominio (outbox) para processamento assincrono.

Por que existe:

- encapsular regra operacional da portaria;
- garantir auditabilidade do ciclo da encomenda.

Principais endpoints:

- `GET /portaria/encomendas`
- `GET /portaria/encomendas/{id}`
- `POST /portaria/encomendas`
- `POST /portaria/encomendas/{id}/retirada`

Persistencia:

- banco `condominio_encomenda` (container `postgres-encomenda`);
- tabela de negocio `encomendas`;
- tabela de integracao `outbox_event`.

---

### 3.4 servico-notificacao (porta 8084 no Docker)

Responsabilidade central:

- consumir eventos de encomenda;
- criar e disponibilizar notificacoes para morador;
- registrar confirmacao de leitura/recebimento.

Por que existe:

- separar processamento assincrono do fluxo transacional da portaria;
- reduzir acoplamento temporal entre receber encomenda e notificar morador.

Principais endpoints:

- `GET /morador/notificacoes`
- `POST /morador/notificacoes/{id}/confirmacao`

Persistencia:

- banco `condominio_notificacao` (container `postgres-notificacao`);
- tabela de negocio `notificacoes`;
- tabela `outbox_event` para padrao de integracao.

Topico principal consumido:

- `encomenda.recebida`.

---

### 3.5 frontend (porta 3000)

Responsabilidade central:

- oferecer interface unificada para morador e funcionario;
- conduzir os fluxos funcionais com autenticacao JWT;
- facilitar a demonstracao operacional ponta a ponta.

Por que existe:

- transformar APIs tecnicas em experiencia de uso orientada a processo;
- permitir validacao funcional ponta a ponta.

Tecnologia:

- React + TypeScript + Vite.

## 4. Diretorios-Chave e Justificativa

### `frontend/`

Para que serve:

- codigo da SPA web (telas, componentes e clientes HTTP).

Por que existe:

- desacopla experiencia de usuario do backend;
- permite evolucao independente da interface.

---

### `infra/`

Para que serve:

- arquivos de infraestrutura de integracao (principalmente Debezium/Kafka Connect).

Por que existe:

- versionar infraestrutura como codigo;
- padronizar setup de CDC entre ambientes.

Exemplo:

- `infra/debezium/encomenda-outbox-connector.config.json`
- `infra/debezium/notificacao-outbox-connector.config.json`

---

### `docs/`

Para que serve:

- contratos OpenAPI e artefatos de documentacao tecnica.

Por que existe:

- garantir transparencia de contrato e rastreabilidade tecnica para evolucao.

---

### `tests-integracao-sistema/`

Para que serve:

- testes cross-module (comportamento entre servicos reais).

Por que existe:

- validar fluxos de negocio acima da unidade de modulo;
- reduzir risco de integracao em producao.

---

### `elk/`

Para que serve:

- pipeline de logs (Filebeat -> Logstash -> Elasticsearch).

Por que existe:

- dar suporte a diagnostico operacional em ambiente distribuido;
- manter trilha de auditoria e investigacao.

## 5. Tecnologias e Ferramentas Utilizadas

Esta secao consolida as tecnologias do projeto por responsabilidade tecnica.

### 5.1 Backend e API

- Java 21
- Spring Boot (camada web e bootstrapping)
- Spring Security Resource Server (autenticacao/autorizacao com JWT)
- Spring Data JPA (persistencia relacional)
- OpenAPI/Swagger (contratos e exploracao de API)

### 5.2 Persistencia e evolucao de schema

- PostgreSQL com banco isolado por servico
- Flyway para versionamento de schema por modulo
- Modelagem de tabelas de negocio + `outbox_event` nos servicos orientados a evento

### 5.3 Mensageria e integracao assincrona

- Apache Kafka (broker de eventos)
- Kafka Connect (runtime de conectores)
- Debezium (CDC a partir do outbox)

### 5.4 Frontend

- React
- TypeScript
- Vite

### 5.5 Observabilidade e operacao

- Micrometer Tracing
- Logs estruturados em JSON
- Filebeat, Logstash, Elasticsearch e Kibana (stack ELK)

### 5.6 Build, execucao e apoio

- Maven multi-modulo
- Docker Compose
- Adminer (inspecao de dados)
- Kafka UI (inspecao de topicos e mensagens)

### 5.7 Qualidade de software

- JUnit 5
- ArchUnit (regras arquiteturais)
- Testcontainers (testes com infraestrutura real)
- RestAssured (testes de API no modulo cross-module)
- Checkstyle, PMD, SpotBugs, JaCoCo e OWASP Dependency-Check

## 6. Desafios Tecnicos Enfrentados e Solucoes Adotadas

### 6.1 Preservar fronteiras da Clean Architecture no dia a dia

Desafio:
- impedir acoplamento acidental de `domain` e `application` com frameworks e adaptadores.

Solucao adotada:
- estrutura em camadas (`domain`, `application`, `adapter/infrastructure`) com dependencias apontando para o nucleo.
- testes ArchUnit em todos os servicos para bloquear dependencias proibidas.

Impacto:
- regras de negocio permanecem testaveis e portaveis, com menor custo de mudanca tecnologica.

### 6.2 Garantir consistencia entre estado de negocio e publicacao de evento

Desafio:
- evitar dual-write (salvar no banco e publicar no broker em passos independentes), que gera inconsistencias em falhas parciais.

Solucao adotada:
- padrao Outbox: persistencia de estado de negocio e `outbox_event` na mesma transacao local.
- Debezium + Kafka Connect para publicar eventos a partir do banco via CDC.

Impacto:
- maior confiabilidade de integracao assincrona sem usar transacao distribuida.

### 6.3 Evoluir schema de banco com seguranca entre ambientes

Desafio:
- manter alinhamento entre entidades, DDL e ambientes (local, Docker e testes) sem drift de schema.

Solucao adotada:
- Flyway habilitado em todos os servicos.
- migracoes versionadas em `db/migration`, com historico rastreavel por modulo.
- JPA configurado sem geracao automatica de DDL em runtime.

Impacto:
- evolucao previsivel de banco, com rollback estrategico por nova migracao e melhor reprodutibilidade.

### 6.4 Validar qualidade alem do teste unitario

Desafio:
- reduzir risco de regressao arquitetural, funcional e nao-funcional em um monorepo com varios servicos.

Solucao adotada:
- estrategia em camadas: testes unitarios/aplicacao, testes web, testes arquiteturais e testes cross-module com Testcontainers.
- quality gate com Checkstyle, PMD, SpotBugs, JaCoCo e OWASP Dependency-Check.

Impacto:
- aumento de confiabilidade evolutiva e deteccao precoce de problemas tecnicos.

### 6.5 Manter diagnostico operacional em ambiente distribuido

Desafio:
- investigar falhas e correlacionar eventos entre servicos distintos.

Solucao adotada:
- logs estruturados com metadados de rastreio.
- centralizacao em ELK para busca e analise temporal unificada.

Impacto:
- troubleshooting mais rapido e melhor rastreabilidade de ponta a ponta.

## 7. Como Rodar o Sistema com Docker

### 7.1 Pre-requisitos

- Docker Desktop (ou Docker Engine + Docker Compose).

### 7.2 Subida completa

No diretorio raiz:

```bash
docker compose up -d --build
```

Conferir status:

```bash
docker compose ps
```

### 7.3 Enderecos principais

- Frontend: `http://localhost:3000`
- Identidade: `http://localhost:8081`
- Usuario: `http://localhost:8082`
- Encomenda: `http://localhost:8083`
- Notificacao: `http://localhost:8084`
- Kibana: `http://localhost:5601`
- Kafka UI: `http://localhost:8086`
- Adminer: `http://localhost:8087`
- Portal de docs: `http://localhost:8090`

### 7.4 Servicos Docker e por que cada um existe

#### Aplicacao

- `frontend`: interface de demonstracao e operacao.
- `servico-identidade`: identidade e JWT.
- `servico-usuario`: cadastro e perfil.
- `servico-encomenda`: fluxo de encomendas e outbox.
- `servico-notificacao`: notificacoes assincronas ao morador.

#### Dados

- `postgres-identidade`: banco exclusivo de identidade.
- `postgres-usuario`: banco exclusivo de usuarios.
- `postgres-encomenda`: banco exclusivo de encomendas.
- `postgres-notificacao`: banco exclusivo de notificacoes.

Motivacao: isolamento por contexto de negocio e menor acoplamento de schema.

#### Mensageria e CDC

- `zookeeper`: coordenacao do cluster Kafka.
- `kafka`: broker de eventos.
- `kafka-connect`: runtime de conectores Debezium.
- `cdc-init`: bootstrap automatico de conectores no startup.

Motivacao: implementar integracao orientada a eventos sem polling manual.

#### Observabilidade

- `filebeat`: coleta logs de containers.
- `logstash`: tratamento e normalizacao de logs.
- `elasticsearch`: indexacao e busca.
- `kibana`: visualizacao e investigacao.

Motivacao: suporte real de operacao e troubleshooting distribuido.

#### Ferramentas de apoio

- `adminer`: inspecao SQL simplificada.
- `kafka-ui`: visualizacao de topicos, mensagens e connectors.
- `docs-site`: entrega de documentacao e JavaDoc.

## 8. Como Rodar os Testes (Detalhado)

### 8.1 Estrategia de testes

O projeto adota validacao em camadas:

- testes unitarios e de aplicacao por modulo;
- testes web/controllers;
- testes de arquitetura (ArchUnit);
- testes cross-module com Testcontainers;
- quality gate estatico.

### 8.2 Suite padrao do monorepo

Executa testes dos modulos Maven:

```bash
mvn test
```

Quando usar:

- verificacao rapida de regressao funcional local.

### 8.3 Testes com Testcontainers

Ativa cenarios que sobem infraestrutura temporaria:

```bash
mvn test -Dtestcontainers.enabled=true
```

Quando usar:

- validacao mais proxima de producao (banco/container real).

### 8.4 Cross-module (foco em integracao de sistema)

```bash
mvn -pl tests-integracao-sistema -am test -Dtestcontainers.enabled=true -Dtest=FluxoCadastroUsuarioCrossModuleTest -Dsurefire.failIfNoSpecifiedTests=false
```

```bash
mvn -pl tests-integracao-sistema -am test -Dtestcontainers.enabled=true -Dtest=FluxoEncomendaNotificacaoRetiradaCrossModuleTest -Dsurefire.failIfNoSpecifiedTests=false
```

O que os testes validam:

- cadastro em `servico-usuario`;
- autenticacao em `servico-identidade`;
- consulta autenticada de perfil em `servico-usuario`.
- recebimento de encomenda por funcionario (`servico-encomenda`);
- consumo de evento `encomenda.recebida` no `servico-notificacao`;
- confirmacao de notificacao por morador e bloqueio de confirmacao por perfil indevido;
- baixa de retirada da encomenda e conferencia de pendencias.

### 8.5 Quality gate

```bash
mvn -Pquality verify
```

Inclui:

- Checkstyle;
- PMD;
- SpotBugs;
- JaCoCo;
- OWASP Dependency-Check.

Execucao mais rapida (sem varredura OWASP):

```bash
mvn -Pquality -DskipTests -Ddependency-check.skip=true verify
```

## 9. Roteiro de Teste Funcional pelo Frontend

Este roteiro foi pensado para execucao ponta a ponta, com inicio, meio e fim claros.

### 9.1 Cenario Morador

1. Abrir `http://localhost:3000`.
2. Entrar em cadastro (`/cadastro`).
3. Criar conta com tipo `MORADOR`.
4. Fazer login.
5. No dashboard, acessar `Notificacoes`.
6. Confirmar notificacoes pendentes (se houver).

### 9.2 Cenario Funcionario

1. Criar conta do tipo `FUNCIONARIO` (via frontend) ou usar conta previamente provisionada.
2. Fazer login como funcionario.
3. Acessar `Receber Encomenda` e registrar nova encomenda.
4. Acessar `Retirar Encomenda` e registrar retirada.

### 9.3 Evidencia de fluxo integrado

Ao registrar recebimento:

- `servico-encomenda` persiste `encomendas` + `outbox_event`;
- Debezium publica evento em Kafka (`encomenda.recebida`);
- `servico-notificacao` processa evento e persiste notificacao;
- morador visualiza e confirma notificacao no frontend.

## 10. Como Visualizar Logs no Kibana

### 10.1 Acesso

- Abrir `http://localhost:5601`.

### 10.2 Descoberta de logs

1. Ir em `Discover`.
2. Selecionar o indice `condominio-logs-*`.
3. Filtrar por servico:
   - `service.name : "servico-identidade"`
   - `service.name : "servico-encomenda"`
   - `service.name : "servico-notificacao"`
4. Ajustar janela temporal para `Last 15 minutes` ou `Last 1 hour`.

### 10.3 O que observar

- `@timestamp`
- `service.name`
- `message`
- `log_level`
- `trace_id` e `span_id` (quando presentes)

## 11. Como Verificar Dados no Banco (Adminer e SQL)

### 11.1 Via Adminer

1. Abrir `http://localhost:8087`.
2. Escolher `PostgreSQL`.
3. Informar servidor, usuario e banco:
   - `postgres-identidade` / `condominio_identidade`
   - `postgres-usuario` / `condominio_usuario`
   - `postgres-encomenda` / `condominio_encomenda`
   - `postgres-notificacao` / `condominio_notificacao`
4. Usuario/senha padrao: `postgres` / `postgres`.

### 11.2 Queries uteis para demonstracao

Identidade:

```sql
SELECT id, username, enabled FROM identity_users ORDER BY username;
```

Usuarios:

```sql
SELECT id, nome_completo, email, tipo, apartamento, bloco
FROM usuarios
ORDER BY id DESC;
```

Encomendas:

```sql
SELECT id, nome_destinatario, apartamento, bloco, status, data_recebimento, data_retirada
FROM encomendas
ORDER BY id DESC;
```

Notificacoes:

```sql
SELECT id, encomenda_id, morador_id, status, created_at, confirmed_at
FROM notificacoes
ORDER BY created_at DESC;
```

## 12. Como Verificar Topicos Kafka

### 12.1 Via Kafka UI

1. Abrir `http://localhost:8086`.
2. Entrar no cluster `condominio`.
3. Verificar topicos de negocio:
   - `encomenda.recebida`
   - `notificacao.evento`
4. Abrir o topico e inspecionar payload e timestamp das mensagens.

### 12.2 Validacao de connectors

```bash
curl http://localhost:8085/connectors
curl http://localhost:8085/connectors/encomenda-outbox-connector/status
curl http://localhost:8085/connectors/notificacao-outbox-connector/status
```

## 13. Documentacao Adicional

- Docker: `DOCKER.md`
- OpenAPI: `docs/openapi`
- JavaDoc (portal): `http://localhost:8090`
- Readme com estrutura de prints: `README-com-prints.md`

## 14. Encerramento

Em sintese, a solucao atende ao objetivo do Tech Challenge ao combinar:

- modelagem de dominio clara;
- separacao arquitetural rigorosa;
- integracao assincrona com resiliencia;
- observabilidade para diagnostico;
- testes multicamadas para confiabilidade evolutiva.

Com isso, o projeto nao apenas "funciona", mas tambem apresenta fundamento tecnico para manutencao e evolucao em contexto real.

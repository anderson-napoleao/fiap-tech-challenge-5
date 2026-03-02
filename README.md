# Sistema Condomínio - Tech Challenge

Plataforma distribuída para gestão condominial, estruturada em microserviços com Clean Architecture, mensageria orientada a eventos e observabilidade centralizada.

Este documento descreve a solução, as decisões de engenharia, a operação via Docker, a estratégia de testes e um roteiro de demonstração ponta a ponta.

## 1. Objetivo do Projeto

Este sistema foi concebido para resolver um problema operacional real de condomínio:

- controlar o cadastro de usuários (moradores e funcionários);
- registrar recebimento e retirada de encomendas;
- gerar notificações para o morador de forma assíncrona;
- manter rastreabilidade da operação com logs estruturados.

Do ponto de vista de engenharia, o foco do projeto é demonstrar:

- separação de responsabilidades por domínio;
- evolução segura com arquitetura limpa;
- consistência entre banco e evento com Outbox + CDC;
- observabilidade realista para ambiente distribuído;
- estratégia de testes em camadas.

## 2. Arquitetura Geral

Arquitetura em microserviços, com responsabilidades bem delimitadas:

- `servico-identidade`: autenticação e emissão de JWT.
- `servico-usuario`: cadastro e manutenção de perfil.
- `servico-encomenda`: ciclo operacional de encomenda (recebimento e retirada).
- `servico-notificacao`: processamento de eventos e notificação ao morador.
- `frontend`: interface web para os fluxos funcionais.

Elementos de suporte:

- PostgreSQL separado por serviço (isolamento de contexto e de schema).
- Kafka + Kafka Connect + Debezium para CDC de eventos de outbox.
- ELK + Filebeat para centralização e análise de logs.

Em termos práticos: cada serviço cuida do seu próprio estado e a integração entre contextos é feita por API e eventos, evitando acoplamento direto de banco.

### 2.1 Decisões arquiteturais (e o motivo)

1. Separação em microserviços por contexto de negócio.
- `servico-identidade`, `servico-usuario`, `servico-encomenda` e `servico-notificacao` evoluem com baixo acoplamento.
- Cada contexto possui API e banco próprios, reduzindo impacto de mudanças locais em outros módulos.

2. Clean Architecture aplicada dentro de cada serviço.
- `domain`: regras de negócio puras, sem framework.
- `application`: casos de uso e portas (entrada/saída), sem Spring.
- `adapter`/`infrastructure`: detalhes técnicos (HTTP, JPA, Kafka, segurança, serialização).
- Regras arquiteturais protegidas por testes ArchUnit em cada módulo.

3. Integração entre contextos por API e eventos.
- Chamadas HTTP cobrem fluxos síncronos (ex.: autenticação e consulta de perfil).
- Eventos cobrem fluxos assíncronos e desacoplam tempo de resposta entre serviços.

4. Consistência entre estado e evento com Outbox + CDC.
- Nos serviços que publicam eventos, estado de negócio e `outbox_event` são persistidos na mesma transação local.
- O Debezium lê o `outbox_event` no Postgres e publica no Kafka sem dual-write na aplicação.

5. Observabilidade como parte da arquitetura.
- Logs estruturados, tracing e stack ELK permitem diagnóstico distribuído sem depender de logs locais isolados.

### 2.2 Versionamento de banco com Flyway

Padrão adotado no projeto:

- Flyway habilitado em todos os serviços (`spring.flyway.enabled=true`).
- Scripts versionados em `src/main/resources/db/migration`.
- Convenção de nomes: `V{numero}__{descricao}.sql`.
- Evolução de schema controlada por migrações; o JPA não é usado para gerar schema em runtime.

Inventário atual de migrações:

| Serviço | Migrações |
| --- | --- |
| `servico-identidade` | `V1__init_schema.sql` |
| `servico-usuario` | `V1__init_schema.sql` |
| `servico-encomenda` | `V1__init_schema.sql`, `V2__add_idx_encomendas_bloco_apto_data.sql` |
| `servico-notificacao` | `V1__init_schema.sql` |

## 3. Serviços da Aplicação (Detalhamento)

### 3.1 `servico-identidade` (porta 8081)

Responsabilidade central:

- autenticar credenciais;
- emitir token JWT;
- administrar identidades e papéis.

Principais endpoints:

- `POST /auth/token`
- `POST /admin/users` (requer `ROLE_ADMIN`; pode retornar `403`)
- `PATCH /admin/users/{id}/disable` (requer `ROLE_ADMIN`; pode retornar `403`)
- `DELETE /admin/users/{id}` (requer `ROLE_ADMIN`; pode retornar `403`)

Persistência:

- banco `condominio_identidade` (container `postgres-identidade`).

### 3.2 `servico-usuario` (porta 8082)

Responsabilidade central:

- cadastrar usuário/morador;
- expor e atualizar perfil autenticado;
- disponibilizar consulta interna de moradores por unidade.

Principais endpoints:

- `POST /users`
- `GET /users/me`
- `PUT /users/me`
- `GET /interno/usuarios/moradores?bloco={bloco}&apartamento={apartamento}` (interno; requer `ROLE_ADMIN`)

Persistência:

- banco `condominio_usuario` (container `postgres-usuario`).

Dependência funcional:

- integração com `servico-identidade` para fluxo de identidade/credencial.

### 3.3 `servico-encomenda` (porta 8083)

Responsabilidade central:

- registrar recebimento de encomenda;
- registrar retirada;
- gerar evento de domínio (outbox) para processamento assíncrono.

Principais endpoints:

- `GET /portaria/encomendas`
- `GET /portaria/encomendas/{id}`
- `POST /portaria/encomendas`
- `POST /portaria/encomendas/{id}/retirada`

Persistência:

- banco `condominio_encomenda` (container `postgres-encomenda`);
- tabela de negócio `encomendas`;
- tabela de integração `outbox_event`.

### 3.4 `servico-notificacao` (porta 8084 no Docker, 8087 local)

Responsabilidade central:

- consumir eventos de encomenda;
- criar e disponibilizar notificações para morador;
- registrar confirmação de leitura/recebimento.

Principais endpoints:

- `GET /morador/notificacoes`
- `POST /morador/notificacoes/{id}/confirmacao`

Persistência:

- banco `condominio_notificacao` (container `postgres-notificacao`);
- tabela de negócio `notificacoes`;
- tabela `outbox_event` para padrão de integração.

Tópico principal consumido:

- `encomenda.recebida`.

### 3.5 `frontend` (porta 3000)

Responsabilidade central:

- oferecer interface unificada para morador e funcionário;
- conduzir os fluxos funcionais com autenticação JWT;
- facilitar a demonstração operacional ponta a ponta.

Tecnologia:

- React + TypeScript + Vite.

## 4. Diretórios-Chave e Justificativa

### `frontend/`

- código da SPA web (telas, componentes e clientes HTTP).

### `infra/`

- infraestrutura de integração (principalmente Debezium/Kafka Connect).
- exemplos: `infra/debezium/encomenda-outbox-connector.config.json` e `infra/debezium/notificacao-outbox-connector.config.json`.

### `docs/`

- contratos OpenAPI e artefatos de documentação técnica.

### `tests-integracao-sistema/`

- testes cross-module (comportamento entre serviços reais).

### `elk/`

- pipeline de logs (Filebeat -> Logstash -> Elasticsearch).

## 5. Tecnologias e Ferramentas Utilizadas

### 5.1 Backend e API

- Java 21
- Spring Boot
- Spring Security Resource Server (JWT)
- Spring Data JPA
- OpenAPI/Swagger

### 5.2 Persistência

- PostgreSQL com banco isolado por serviço
- Flyway para versionamento de schema

### 5.3 Mensageria

- Apache Kafka
- Kafka Connect
- Debezium

### 5.4 Frontend

- React
- TypeScript
- Vite

### 5.5 Observabilidade

- Micrometer Tracing
- Logs estruturados em JSON
- Filebeat, Logstash, Elasticsearch e Kibana

### 5.6 Build e apoio

- Maven multi-módulo
- Docker Compose
- Adminer
- Kafka UI

### 5.7 Qualidade de software

- JUnit 5
- ArchUnit
- Testcontainers
- RestAssured
- Checkstyle, PMD, SpotBugs, JaCoCo e OWASP Dependency-Check

## 6. Como Rodar o Sistema com Docker

### 6.1 Pré-requisitos

- Docker Desktop (ou Docker Engine + Docker Compose).

### 6.2 Subida completa

```bash
docker compose up -d --build
docker compose ps
```

### 6.3 Endereços principais

- Frontend: `http://localhost:3000`
- Identidade: `http://localhost:8081`
- Usuário: `http://localhost:8082`
- Encomenda: `http://localhost:8083`
- Notificação: `http://localhost:8084` (Docker) / `http://localhost:8087` (local)
- Kibana: `http://localhost:5601`
- Kafka UI: `http://localhost:8086`
- Adminer: `http://localhost:8087`
- Portal de documentação: `http://localhost:8090`

### 6.4 Validação dos conectores CDC

```bash
curl http://localhost:8085/connectors
curl http://localhost:8085/connectors/encomenda-outbox-connector/status
curl http://localhost:8085/connectors/notificacao-outbox-connector/status
```

## 7. Como Rodar os Testes

### 7.1 Suite padrão do monorepo

```bash
mvn test
```

### 7.2 Cross-module (integração de sistema)

```bash
mvn -pl tests-integracao-sistema -am test -Dtestcontainers.enabled=true -Dtest=FluxoCadastroUsuarioCrossModuleTest -Dsurefire.failIfNoSpecifiedTests=false
```

PowerShell (Windows):

```powershell
mvn -pl tests-integracao-sistema -am test "-Dtestcontainers.enabled=true" "-Dtest=FluxoCadastroUsuarioCrossModuleTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

```bash
mvn -pl tests-integracao-sistema -am test -Dtestcontainers.enabled=true -Dtest=FluxoEncomendaNotificacaoRetiradaCrossModuleTest -Dsurefire.failIfNoSpecifiedTests=false
```

PowerShell (Windows):

```powershell
mvn -pl tests-integracao-sistema -am test "-Dtestcontainers.enabled=true" "-Dtest=FluxoEncomendaNotificacaoRetiradaCrossModuleTest" "-Dsurefire.failIfNoSpecifiedTests=false"
```

### 7.3 Quality gate

Execução mais rápida (sem varredura OWASP):

```bash
mvn -Pquality -DskipTests -Ddependency-check.skip=true verify
```

PowerShell (Windows):

```powershell
mvn -Pquality '-DskipTests=true' '-Ddependency-check.skip=true' verify
```

## 8. Roteiro de Demonstração Funcional (Frontend)

### 8.1 Cenário morador

1. Acesse `http://localhost:3000`.
2. Vá para cadastro (`/cadastro`).
3. Crie conta com tipo `MORADOR`.
4. Faça login.
5. No dashboard, acesse notificações.
6. Confirme notificações pendentes (quando houver).

### 8.2 Cenário funcionário

1. Crie conta com tipo `FUNCIONARIO` (ou use uma já provisionada).
2. Faça login como funcionário.
3. Acesse `Receber Encomenda` e registre uma nova encomenda.
4. Acesse `Retirar Encomenda` e registre a retirada.

### 8.3 Evidência de fluxo integrado

Ao registrar recebimento:

- `servico-encomenda` persiste `encomendas` + `outbox_event`;
- Debezium publica evento em Kafka (`encomenda.recebida`);
- `servico-notificacao` processa evento e persiste notificação;
- morador visualiza e confirma notificação no frontend.

## 9. Observabilidade (Kibana)

1. Acesse `http://localhost:5601`.
2. Abra `Discover`.
3. Selecione o índice `condominio-logs-*`.
4. Filtre por serviço:
   - `service.name : "servico-identidade"`
   - `service.name : "servico-encomenda"`
   - `service.name : "servico-notificacao"`

Campos úteis para análise:

- `@timestamp`
- `service.name`
- `message`
- `log_level`
- `trace_id` e `span_id`

## 10. Verificação de Dados no Banco (Adminer)

1. Acesse `http://localhost:8087`.
2. Escolha `PostgreSQL`.
3. Conecte no banco desejado:
   - `postgres-identidade` / `condominio_identidade`
   - `postgres-usuario` / `condominio_usuario`
   - `postgres-encomenda` / `condominio_encomenda`
   - `postgres-notificacao` / `condominio_notificacao`
4. Usuário/senha padrão: `postgres` / `postgres`.

Consultas úteis:

```sql
SELECT id, username, enabled FROM identity_users ORDER BY username;
```

```sql
SELECT id, nome_completo, email, tipo, apartamento, bloco
FROM usuarios
ORDER BY id DESC;
```

```sql
SELECT id, nome_destinatario, apartamento, bloco, status, data_recebimento, data_retirada
FROM encomendas
ORDER BY id DESC;
```

```sql
SELECT id, encomenda_id, morador_id, status, created_at, confirmed_at
FROM notificacoes
ORDER BY created_at DESC;
```

## 11. Verificação de Tópicos Kafka

1. Acesse `http://localhost:8086`.
2. Abra o cluster `condominio`.
3. Verifique os tópicos de negócio:
   - `encomenda.recebida`
   - `notificacao.evento`
4. Inspecione payload e timestamp das mensagens.

## 12. Documentação Adicional

- Docker: `DOCKER.md`
- OpenAPI: `docs/openapi`
- Portal de documentação/Javadoc: `http://localhost:8090`

## 13. Encerramento

Em síntese, a solução atende ao objetivo do Tech Challenge ao combinar:

- modelagem de domínio clara;
- separação arquitetural rigorosa;
- integração assíncrona com resiliência;
- observabilidade para diagnóstico;
- testes multicamadas para confiabilidade evolutiva.

Com isso, o projeto não apenas funciona, mas também apresenta fundamento técnico para manutenção e evolução em contexto real.

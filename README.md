# Sistema Condomínio

Plataforma de gestão condominial baseada em microserviços, com foco em arquitetura limpa, rastreabilidade e qualidade de software.

## Como executar e usar

### Pré-requisitos por cenário

#### 1. Rodar somente com Docker (recomendado para uso)

- Docker Desktop (ou Docker Engine com Docker Compose)
- PowerShell 7+ no Windows (opcional, para usar `start.ps1`)

📌 Nesse cenário, **Java e Maven no host não são obrigatórios**.

#### 2. Desenvolver localmente e rodar testes com Maven

- Java 21
- Maven 3.9+
- Docker funcional (necessário para Testcontainers e stack de apoio)

### Subir aplicação completa

No diretório raiz:

```powershell
./start.ps1
```

Alternativa manual:

```powershell
docker compose up -d
```

### Acessos principais

- Frontend: `http://localhost:3000`
- Serviço Identidade: `http://localhost:8081`
- Serviço Usuário: `http://localhost:8082`
- Serviço Encomenda: `http://localhost:8083`
- Serviço Notificação: `http://localhost:8084` (Docker)

### OpenAPI e Swagger

- Identidade: `http://localhost:8081/swagger-ui/index.html`
- Usuário: `http://localhost:8082/swagger-ui/index.html`
- Encomenda: `http://localhost:8083/swagger-ui/index.html`
- Notificação: `http://localhost:8084/swagger-ui/index.html` (Docker) / `8087` (profile local)

### JavaDoc e Docker Docs

- Portal de documentação técnica: `http://localhost:8090`
- JavaDoc Identidade: `http://localhost:8090/javadocs/servico-identidade/index.html`
- JavaDoc Usuário: `http://localhost:8090/javadocs/servico-usuario/index.html`
- JavaDoc Encomenda: `http://localhost:8090/javadocs/servico-encomenda/index.html`
- JavaDoc Notificação: `http://localhost:8090/javadocs/servico-notificacao/index.html`
- Guia Docker: `http://localhost:8090/DOCKER.md`

### Fluxo básico de uso

1. Criar usuário no frontend (morador).
2. Fazer login para obter token JWT.
3. Registrar recebimento e retirada de encomendas com perfil de funcionário.
4. Consultar e confirmar notificações pendentes com perfil de morador.

## Como rodar todos os testes

Pré-requisitos para testes:

- `mvn test` e `mvn -Pquality ...`: Java 21 + Maven 3.9+
- testes com Testcontainers: Java 21 + Maven 3.9+ + Docker funcional

### Suite padrão (unitários + web + arquitetura)

```powershell
mvn test
```

### Testes integrados com Testcontainers

```powershell
mvn test '-Dtestcontainers.enabled=true'
```

### Apenas teste integrado cross-module

```powershell
mvn -pl tests-integracao-sistema -am test '-Dtestcontainers.enabled=true' -Dtest=FluxoCadastroUsuarioCrossModuleTest '-Dsurefire.failIfNoSpecifiedTests=false'
```

### Qualidade estática (quality gate)

```powershell
mvn -Pquality verify
```

Execução local mais rápida (sem OWASP):

```powershell
mvn -Pquality -DskipTests '-Ddependency-check.skip=true' verify
```

## Relatório técnico do sistema

## Visão geral

O sistema implementa uma arquitetura distribuída orientada a serviços para suporte às operações de portaria e relacionamento com moradores.

- `servico-identidade`: serviço responsável por autenticação, autorização e emissão de tokens JWT utilizados pelos demais módulos.
- `servico-usuario`: serviço responsável pelo ciclo de vida cadastral do usuário, incluindo dados pessoais e dados residenciais.
- `servico-encomenda`: serviço responsável pelo registro de recebimento e baixa de retirada de encomendas, com regras de domínio da portaria.
- `servico-notificacao`: serviço responsável pela geração, persistência e confirmação de notificações associadas a eventos de encomenda.
- `frontend`: camada de apresentação web para execução dos fluxos operacionais por funcionário e morador.
- `tests-integracao-sistema`: módulo dedicado à validação de comportamento integrado entre serviços, banco de dados e contratos de API.

## Modelo arquitetônico

O backend adota Clean Architecture como modelo de organização estrutural e de governança de dependências.

- `domain`: concentra entidades e regras de negócio centrais, sem dependência de frameworks ou infraestrutura.
- `application`: concentra casos de uso e contratos (portas), coordenando o domínio sem acoplamento a detalhes tecnológicos.
- `adapter` e `infrastructure`: implementam mecanismos de entrada e saída (HTTP, persistência, segurança, mensageria e observabilidade).

As dependências são orientadas para o núcleo, de modo que decisões de framework, banco ou transporte possam evoluir sem comprometer a lógica de negócio.

## Tecnologias e ferramentas

### Backend e APIs

- Java 21
- Spring Boot 3.3
- Spring Security + Resource Server
- Spring Data JPA
- PostgreSQL
- Flyway
- OpenAPI/Swagger (springdoc)

### Frontend

- React 18
- TypeScript
- Vite
- React Router
- React Query
- React Hook Form
- Axios
- Tailwind CSS
- Nginx (entrega da SPA em container)

### Mensageria e observabilidade

- Kafka + Kafka Connect + Debezium (CDC/outbox)
- Micrometer Tracing
- Logback JSON (logs estruturados)
- ELK (Elasticsearch, Logstash, Kibana) + Filebeat

### Build e execução

- Maven multi-módulo
- Docker Compose

### Qualidade e testes

- JUnit 5
- ArchUnit
- Testcontainers
- RestAssured (cross-module)
- Checkstyle
- PMD
- SpotBugs
- JaCoCo
- OWASP Dependency-Check

## Desafios técnicos e soluções adotadas

1. Consistência transacional entre estado de negócio e publicação de evento.
- Problema técnico: evitar divergência entre dados persistidos e mensagens emitidas.
- Solução adotada: padrão Outbox com CDC (Debezium/Kafka Connect), preservando atomicidade na escrita e entrega assíncrona confiável.

2. Migração de persistência para cenário aderente a produção.
- Problema técnico: limitações de banco em memória para validação de comportamento real.
- Solução adotada: padronização em PostgreSQL com versionamento de esquema via Flyway por serviço.

3. Controle de acesso por papel em fronteiras de API.
- Problema técnico: garantir segregação funcional entre operações de funcionário e morador.
- Solução adotada: políticas explícitas com `ROLE_FUNCIONARIO` e `ROLE_MORADOR` nos serviços protegidos por JWT.

4. Rastreabilidade e diagnóstico em ambiente distribuído.
- Problema técnico: correlação de falhas entre múltiplos serviços.
- Solução adotada: logging estruturado em JSON com `trace_id` e `span_id`, integrado à stack ELK para análise operacional.

5. Confiabilidade de integração entre módulos.
- Problema técnico: reduzir falso positivo de testes isolados frente ao comportamento real.
- Solução adotada: testes integrados com Testcontainers e suíte cross-module para validação ponta a ponta.

## Ênfase em qualidade de software

A estratégia de qualidade foi estruturada para combinar correção funcional, aderência arquitetural e mantenabilidade evolutiva.

- Verificação em múltiplos níveis: testes de domínio, aplicação, web e integração.
- Proteção arquitetural: regras automatizadas com ArchUnit para preservar fronteiras da Clean Architecture.
- Realismo de execução: uso de Testcontainers para validar persistência e integração em ambiente próximo ao operacional.
- Governança estática: análise com Checkstyle, PMD, SpotBugs, JaCoCo e OWASP Dependency-Check no profile Maven `quality`.
- Rastreabilidade técnica: observabilidade com logs estruturados e correlação distribuída para apoiar investigação de defeitos.

## Documentação complementar

- OpenAPI: `docs/openapi`
- Operação Docker: `DOCKER.md`

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

### Smoke test CDC

```powershell
./scripts/smoke-test-encomenda-cdc.ps1 -StartInfra
```

## Relatório técnico do sistema

## Visão geral

O sistema é composto por:

- `servico-identidade`: autenticação, emissão de JWT e gestão administrativa de identidades
- `servico-usuario`: cadastro e manutenção de perfil de moradores
- `servico-encomenda`: recebimento e baixa de retirada de encomendas na portaria
- `servico-notificacao`: processamento e confirmação de notificações para moradores
- `frontend`: interface web para os fluxos de negócio
- `tests-integracao-sistema`: validação ponta a ponta entre módulos

## Modelo arquitetônico

O backend aplica Clean Architecture com separação de responsabilidades:

- `domain`: regras centrais e entidades ricas, sem framework
- `application`: casos de uso e portas de entrada/saída
- `adapter`/`infrastructure`: HTTP, persistência, segurança, observabilidade e integrações

Princípios aplicados:

- dependências apontando para dentro
- regras de negócio isoladas de frameworks
- integração externa mediada por portas para preservar testabilidade

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
- GitHub Actions (`build-test`, `integration-testcontainers`, `quality`)

## Desafios técnicos e soluções adotadas

1. Consistência entre escrita de negócio e publicação de evento.
- Desafio: evitar perda de evento entre transação de domínio e mensageria.
- Solução: padrão Outbox + CDC com Debezium/Kafka Connect.

2. Evolução de persistência para ambiente mais realista.
- Desafio: sair de cenários simplificados (H2 e store em memória).
- Solução: migração para PostgreSQL com Flyway consolidado por módulo.

3. Segurança por papel em APIs de negócio.
- Desafio: garantir autorização coerente por contexto funcional.
- Solução: regras de `ROLE_FUNCIONARIO` e `ROLE_MORADOR` nos serviços consumidores de JWT.

4. Observabilidade em múltiplos serviços.
- Desafio: diagnosticar erros e rastrear requisições distribuídas.
- Solução: logs JSON padronizados com `trace_id`/`span_id` e stack ELK.

5. Testes integrados confiáveis entre módulos.
- Desafio: validar interação real entre serviços e banco.
- Solução: Testcontainers nos testes de integração e módulo dedicado cross-module.

## Ênfase em qualidade de software

- testes de domínio, aplicação e web por módulo
- testes de arquitetura (ArchUnit) para proteção de camadas
- testes integrados com banco real via Testcontainers
- testes cross-module para fluxos ponta a ponta
- análise estática no profile Maven `quality`
- gate de CI para reduzir regressão antes de merge

## Documentação complementar

- OpenAPI: `docs/openapi`
- Qualidade: `docs/qualidade-*.md`
- Operação Docker: `DOCKER.md`
- Observabilidade: `docs/observabilidade-*.md`

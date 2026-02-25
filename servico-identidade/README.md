# servico-identidade

## Resumo

Microserviço responsável por autenticação e autorização base do sistema.

Responsabilidades:

- emitir token JWT (`/auth/token`)
- criar, desabilitar e remover identidades (`/admin/users`)
- persistir credenciais e papéis em PostgreSQL

## Endpoints principais

- `POST /auth/token`
- `POST /admin/users`
- `PATCH /admin/users/{id}/disable`
- `DELETE /admin/users/{id}`
- `GET /actuator/health`

## Stack

- Java 21
- Spring Boot Web
- Spring Security (Basic + JWT)
- Spring Data JPA
- PostgreSQL + Flyway
- OpenAPI/Swagger
- Micrometer Tracing + Logback JSON

## Clean Architecture

- `domain`: regras de identidade sem framework
- `application`: casos de uso e portas
- `adapter`/`infrastructure`: HTTP, segurança e persistência

Qualidade arquitetural protegida por ArchUnit.

## Desafios e soluções

1. Evoluir de store em memória para persistência real.
- Solução: `PostgresUsuarioStore` + entidades JPA + Flyway.

2. Padronizar emissão/consumo de JWT.
- Solução: configuração central de `issuer` e `secret`.

3. Melhorar rastreabilidade operacional.
- Solução: logs estruturados com `trace_id` e `span_id`.

## Execução

Pré-requisito: PostgreSQL em `localhost:5431` (ou variáveis `DB_*`).

```powershell
mvn -pl servico-identidade spring-boot:run
```

Swagger: `http://localhost:8081/swagger-ui/index.html`

## Testes

```powershell
mvn -pl servico-identidade test
```

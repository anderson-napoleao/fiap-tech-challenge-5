# servico-usuario

## Resumo

Microserviço responsável por cadastro e manutenção de perfil de usuário/morador.

Responsabilidades:

- cadastrar usuário (`POST /users`)
- consultar perfil autenticado (`GET /users/me`)
- atualizar perfil autenticado (`PUT /users/me`)

## Endpoints principais

- `POST /users` (público)
- `GET /users/me` (autenticado)
- `PUT /users/me` (autenticado)
- `GET /actuator/health`

## Stack

- Java 21
- Spring Boot Web
- Spring Security Resource Server
- Spring Data JPA
- PostgreSQL + Flyway
- OpenAPI/Swagger
- Micrometer Tracing + Logback JSON

## Clean Architecture

- `domain`: entidade e regras de usuário
- `application`: casos de uso de cadastro/perfil e portas
- `adapter`/`infrastructure`: API HTTP, segurança e persistência

Qualidade arquitetural protegida por ArchUnit.

## Desafios e soluções

1. Cadastro sincronizado com identidade.
- Solução: integração via porta com `servico-identidade`.

2. Preservar regras de negócio fora do framework.
- Solução: validações em comandos de casos de uso e domínio puro.

3. Suporte operacional.
- Solução: logs estruturados e tracing distribuído.

## Execução

Pré-requisitos:

- PostgreSQL em `localhost:5434`
- `servico-identidade` em `http://localhost:8081`

```powershell
mvn -pl servico-usuario spring-boot:run
```

Swagger: `http://localhost:8082/swagger-ui/index.html`

## Testes

```powershell
mvn -pl servico-usuario test
```

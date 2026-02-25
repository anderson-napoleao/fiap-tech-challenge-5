# servico-notificacao

## Resumo

Microserviço responsável por notificações para moradores com base nos eventos de encomenda.

Responsabilidades:

- processar eventos de outbox/CDC
- listar notificações pendentes ou confirmadas do morador
- confirmar recebimento de notificação

## Endpoints principais

- `GET /morador/notificacoes` (`ROLE_MORADOR`)
- `POST /morador/notificacoes/{id}/confirmacao` (`ROLE_MORADOR`)
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

- `domain`: regras de notificação e status
- `application`: casos de uso e portas de entrada de eventos
- `adapter`/`infrastructure`: API HTTP, persistência, segurança e processamento

Qualidade arquitetural protegida por ArchUnit.

## Desafios e soluções

1. Idempotência no processamento de eventos.
- Solução: estratégia defensiva de processamento e controle de status.

2. Segurança por perfil.
- Solução: endpoints de morador protegidos por `ROLE_MORADOR`.

3. Auditoria operacional.
- Solução: logs estruturados com contexto e tracing.

## Execução

Pré-requisitos:

- PostgreSQL em `localhost:5433`
- JWT válido emitido pelo `servico-identidade`

```powershell
mvn -pl servico-notificacao spring-boot:run
```

Swagger:

- Local profile: `http://localhost:8087/swagger-ui/index.html`
- Docker profile: `http://localhost:8084/swagger-ui/index.html`

## Testes

Suite padrão:

```powershell
mvn -pl servico-notificacao test
```

Com Testcontainers:

```powershell
mvn -pl servico-notificacao test '-Dtestcontainers.enabled=true'
```

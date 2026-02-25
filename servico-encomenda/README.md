# servico-encomenda

## Resumo

Microserviço responsável pelo ciclo de encomendas na portaria.

Responsabilidades:

- registrar recebimento de encomenda
- registrar baixa de retirada
- publicar evento de domínio via Outbox

## Endpoints principais

- `GET /portaria/encomendas` (`ROLE_FUNCIONARIO`) com paginacao e filtros `apartamento`/`bloco`/`data` (YYYY-MM-DD)
- `GET /portaria/encomendas/{id}` (`ROLE_FUNCIONARIO`)
- `POST /portaria/encomendas` (`ROLE_FUNCIONARIO`)
- `POST /portaria/encomendas/{id}/retirada` (`ROLE_FUNCIONARIO`)
- `GET /actuator/health`

## Stack

- Java 21
- Spring Boot Web
- Spring Security Resource Server
- Spring Data JPA
- PostgreSQL + Flyway
- Outbox pattern
- OpenAPI/Swagger
- Micrometer Tracing + Logback JSON

## Clean Architecture

- `domain`: entidade `Encomenda` e regras de estado
- `application`: casos de uso e portas
- `adapter`/`infrastructure`: API HTTP, segurança, persistência e outbox

Qualidade arquitetural protegida por ArchUnit.

## Desafios e soluções

1. Consistência entre estado de negócio e evento.
- Solução: persistência de encomenda + outbox na mesma transação.

2. Controle de acesso da portaria.
- Solução: proteção por `ROLE_FUNCIONARIO`.

3. Auditoria de retirada.
- Solução: persistência explícita de `retiradoPorNome`.

## Execução

Pré-requisitos:

- PostgreSQL em `localhost:5432`
- JWT válido emitido pelo `servico-identidade`

```powershell
mvn -pl servico-encomenda spring-boot:run
```

Swagger: `http://localhost:8083/swagger-ui/index.html`

## Testes

Suite padrão:

```powershell
mvn -pl servico-encomenda test
```

Com Testcontainers:

```powershell
mvn -pl servico-encomenda test '-Dtestcontainers.enabled=true'
```

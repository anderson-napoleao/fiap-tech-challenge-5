# OpenAPI - Sistema Condominio

Este diretorio concentra os contratos OpenAPI detalhados de cada microservico.

## Arquivos

- `servico-identidade.openapi.yaml`
- `servico-usuario.openapi.yaml`
- `servico-encomenda.openapi.yaml`
- `servico-notificacao.openapi.yaml`

## Como visualizar via Spring Boot

Os endpoints de documentacao estao habilitados sem restricao por profile.

1. Suba o servico desejado.
2. Acesse o JSON OpenAPI em `/v3/api-docs`.
3. Acesse a UI Swagger em `/swagger-ui/index.html`.

### Endpoints por servico

- `servico-identidade` (porta `8081`)
  - `http://localhost:8081/v3/api-docs`
  - `http://localhost:8081/swagger-ui/index.html`
- `servico-usuario` (porta `8082`)
  - `http://localhost:8082/v3/api-docs`
  - `http://localhost:8082/swagger-ui/index.html`
- `servico-encomenda` (porta `8083`)
  - `http://localhost:8083/v3/api-docs`
  - `http://localhost:8083/swagger-ui/index.html`
- `servico-notificacao` (porta `8087`)
  - `http://localhost:8087/v3/api-docs`
  - `http://localhost:8087/swagger-ui/index.html`

## Como usar autenticacao na Swagger UI

- Endpoints JWT: gere token em `servico-identidade` (`POST /auth/token`) e use `Authorize` com `Bearer <token>`.
- Endpoints admin de identidade: use autenticacao Basic (`admin/admin` no ambiente local padrao).

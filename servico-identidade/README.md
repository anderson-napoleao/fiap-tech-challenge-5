# servico-identidade

Servico de identidade com JWT simples (HS256) para ambiente local.

## Requisitos

- Java 21
- Maven 3.9+

## Como subir

No diretorio raiz do repositorio:

```bash
mvn -pl servico-identidade spring-boot:run
```

## Endpoints principais

- Health: `GET /actuator/health`
- Criar usuario: `POST /admin/users`
- Remover usuario: `DELETE /admin/users/{id}`
- Desabilitar usuario: `PATCH /admin/users/{id}/disable`
- Gerar token: `POST /auth/token`

## Criar usuario

```bash
curl -i -X POST "http://localhost:8081/admin/users" \
  -H "Content-Type: application/json" \
  -d '{
    "email":"maria@teste.com",
    "password":"123456",
    "role":"USER"
  }'
```

## Gerar token JWT

```bash
curl -i -X POST "http://localhost:8081/auth/token" \
  -H "Content-Type: application/json" \
  -d '{
    "username":"maria@teste.com",
    "password":"123456"
  }'
```

Resposta:

```json
{
  "access_token": "<jwt>",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

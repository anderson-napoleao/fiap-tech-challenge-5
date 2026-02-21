# servico-identidade

Authorization Server OAuth2/OIDC com Spring Authorization Server, emissão JWT (RS256), discovery OIDC e JWKS.

## Requisitos

- Java 21
- Maven 3.9+

## Como subir

No diretório raiz do repositório:

```bash
mvn -pl servico-identidade spring-boot:run
```

## Endpoints principais

- Discovery OIDC: `GET /.well-known/openid-configuration`
- JWKS: `GET /oauth2/jwks`
- Health: `GET /actuator/health`

## Criar usuário em runtime (`/admin/users`)

Endpoint protegido com **Basic Auth**.

Credenciais admin padrão (dev):
- usuário: `admin`
- senha: `admin`

Exemplo:

```bash
curl -i -X POST "http://localhost:8080/admin/users" \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "username":"maria",
    "password":"123456",
    "roles":["MORADOR","PORTEIRO"]
  }'
```

As roles recebidas viram authorities `ROLE_...` internamente.

## Testar login humano no browser (Authorization Code + PKCE)

Client registrado em memória:
- `client_id`: `web`
- `client_secret`: `web-secret`
- grant types: `authorization_code`, `refresh_token`
- redirect URI: `http://localhost:8080/login/oauth2/code/web`
- scopes: `openid`, `profile`
- PKCE: habilitado (`requireProofKey=true`)

Abra no browser:

```text
http://localhost:8080/oauth2/authorize?response_type=code&client_id=web&scope=openid%20profile&redirect_uri=http://localhost:8080/login/oauth2/code/web&code_challenge=abcdefghijklmnopqrstuvwxyzABCDEFGH&code_challenge_method=S256
```

Faça login com um usuário existente em memória (ex.: criado via `/admin/users`) e autorize.

## Trocar authorization code por token

```bash
curl -i -X POST "http://localhost:8080/oauth2/token" \
  -u web:web-secret \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=<AUTHORIZATION_CODE>" \
  -d "redirect_uri=http://localhost:8080/login/oauth2/code/web" \
  -d "code_verifier=<CODE_VERIFIER_USADO_NO_PKCE>"
```

O access token retornado é JWT assinado com chave RSA em memória (RS256).

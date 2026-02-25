# Qualidade - Testes Integrados

## Escopo atual

- Testes de integracao de modulo com Testcontainers JDBC:
  - `servico-encomenda` (`*IntegrationTest`, profile `integration`)
  - `servico-notificacao` (`*IntegrationTest`, profile `integration`)
- Teste cross-module de sistema com `@Testcontainers`:
  - `tests-integracao-sistema/src/test/java/br/com/condominio/sistema/integracao/FluxoCadastroUsuarioCrossModuleTest.java`

Todos os testes de Testcontainers estao condicionados a:

- `-Dtestcontainers.enabled=true`

Sem essa propriedade, eles ficam `skipped` no `mvn test`.

## Pre-requisitos

- Docker ativo e acessivel (`docker info`).
- Java 21.
- Maven 3.9+.

## Execucao local

Suite padrao (sem Testcontainers):

```powershell
mvn test
```

Suite com Testcontainers habilitado:

```powershell
mvn test '-Dtestcontainers.enabled=true'
```

Apenas fluxo cross-module:

```powershell
mvn -pl tests-integracao-sistema -am test '-Dtestcontainers.enabled=true' -Dtest=FluxoCadastroUsuarioCrossModuleTest '-Dsurefire.failIfNoSpecifiedTests=false'
```

## Fluxo validado no teste cross-module

1. sobe `servico-identidade` e `servico-usuario` em portas randomicas;
2. cria usuario em `/users` (servico-usuario chama servico-identidade internamente);
3. autentica em `/auth/token`;
4. consulta `/users/me` com Bearer token.

## CI

Workflow: `.github/workflows/ci.yml`

- job `integration-testcontainers` executa: `mvn -B test -Dtestcontainers.enabled=true`.

## Troubleshooting

Se ocorrer `Could not find a valid Docker environment`:

1. validar `docker info` no mesmo terminal da execucao Maven;
2. reiniciar Docker Desktop;
3. executar em ambiente Linux/CI (GitHub Actions) quando houver incompatibilidade local entre Docker Desktop e Testcontainers.

# Qualidade - CI Gates

## Pipeline

Arquivo: `.github/workflows/ci.yml`

Jobs atuais:

1. `build-test`
2. `integration-testcontainers`
3. `quality`

## O que cada gate valida

1. `build-test`
- compila e executa `mvn -B test` (suite padrao).

2. `integration-testcontainers`
- valida testes integrados com Docker/Testcontainers:
`mvn -B test -Dtestcontainers.enabled=true`.

3. `quality`
- valida analise estatica no profile de qualidade:
`mvn -B -Pquality -DskipTests verify`.

## Politica de merge

Para bloquear merge automaticamente:

1. habilitar branch protection no repositorio;
2. marcar os 3 jobs como required checks.

## Observacao operacional

O job `integration-testcontainers` depende de ambiente com Docker funcional.

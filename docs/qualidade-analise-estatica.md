# Qualidade - Analise Estatica

## Profile Maven

Foi adicionado profile `quality` no `pom.xml` raiz com:

- Checkstyle
- PMD
- SpotBugs
- JaCoCo
- OWASP Dependency-Check

## Execucao

```powershell
mvn -Pquality verify
```

Execucao sem testes:

```powershell
mvn -Pquality -DskipTests verify
```

Execucao sem OWASP (uso local rapido):

```powershell
mvn -Pquality -DskipTests '-Ddependency-check.skip=true' verify
```

Observacao: a primeira execucao com OWASP pode demorar bastante por download/atualizacao de base.

## Arquivos de configuracao

- `config/checkstyle/checkstyle.xml`
- `config/pmd/ruleset.xml`

## CI

Workflow: `.github/workflows/ci.yml`

- job `build-test`: `mvn test`
- job `quality`: `mvn -Pquality -DskipTests verify`

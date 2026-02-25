# tests-integracao-sistema

## Resumo

Módulo dedicado para testes integrados cross-module.

Objetivo:

- validar fluxos ponta a ponta entre serviços, banco real e contratos HTTP

Cenário atual coberto:

- cadastro de usuário no `servico-usuario`
- autenticação no `servico-identidade`
- consulta de perfil autenticado no `servico-usuario`

## Tecnologias e ferramentas

- JUnit 5
- RestAssured
- Testcontainers (PostgreSQL)
- Spring Boot Test

## Papel arquitetônico

Este módulo não implementa domínio; ele valida integração real entre módulos do sistema.

## Desafios e soluções

1. Isolar banco por execução de teste.
- Solução: containers PostgreSQL temporários.

2. Evitar quebra do build local sem Docker pronto.
- Solução: testes condicionados por `testcontainers.enabled=true`.

3. Validar comunicação real serviço a serviço.
- Solução: subida de aplicações em portas randômicas e chamadas HTTP reais.

## Execução

Suite padrão (teste fica `skipped`):

```powershell
mvn -pl tests-integracao-sistema test
```

Com Testcontainers habilitado:

```powershell
mvn -pl tests-integracao-sistema -am test '-Dtestcontainers.enabled=true' -Dtest=FluxoCadastroUsuarioCrossModuleTest '-Dsurefire.failIfNoSpecifiedTests=false'
```

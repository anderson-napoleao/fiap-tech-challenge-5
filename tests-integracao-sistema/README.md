# tests-integracao-sistema

## Resumo

Módulo dedicado para testes integrados cross-module.

Objetivo:

- validar fluxos ponta a ponta entre serviços, banco real e contratos HTTP

Cenário atual coberto:

- cadastro de usuário no `servico-usuario`
- autenticação no `servico-identidade`
- consulta de perfil autenticado no `servico-usuario`
- recebimento de encomenda no `servico-encomenda` por funcionário
- consumo de evento `encomenda.recebida` no `servico-notificacao`
- listagem e confirmação de notificação por morador
- baixa de retirada da encomenda no `servico-encomenda`

## Tecnologias e ferramentas

- JUnit 5
- RestAssured
- Testcontainers (PostgreSQL e Kafka)
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

Observacao: os testes sao automaticamente marcados como `skipped` quando o Docker nao estiver disponivel no ambiente.

Fluxo ponta a ponta encomenda/notificação/retirada:

```powershell
mvn -pl tests-integracao-sistema -am test '-Dtestcontainers.enabled=true' -Dtest=FluxoEncomendaNotificacaoRetiradaCrossModuleTest '-Dsurefire.failIfNoSpecifiedTests=false'
```

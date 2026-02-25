# Backlog Tecnico - Testes Integrados e Analise de Codigo

## Objetivo

Implementar um gate de qualidade para o sistema com duas frentes:
- testes integrados entre modulos para validar interacao real entre servicos;
- analise estatica automatizada para reduzir risco de regressao, violacao arquitetural e vulnerabilidade.

## Baseline Atual

- Ja existe cobertura de arquitetura com ArchUnit nos 4 servicos.
- Ja existem testes de integracao locais em `servico-encomenda` e `servico-notificacao`.
- Ja existe script de smoke CDC: `scripts/smoke-test-encomenda-cdc.ps1`.
- Ainda nao existe pipeline CI versionado no repositorio para gate de qualidade.
- Ainda nao existem plugins padrao de analise estatica (Checkstyle, SpotBugs, PMD, OWASP) no build.

## Convencoes Globais

- Nao violar Clean Architecture (domain puro; application sem framework).
- Teste integrado deve validar fluxo entre modulos, nao apenas classe isolada.
- Falha de qualidade deve quebrar build no `mvn verify`.
- Primeiro garantir um MVP executavel e depois endurecer metas (cobertura e regras).

## Fase 1 - Suite Integrada Minima no Build

### QLT-001 - Integrar testes de integracao existentes no fluxo de validacao

- Tipo: Chore de qualidade
- Prioridade: Alta
- Estimativa: 1 dia
- Dependencias: nenhuma
- Evidencia esperada:
  - `servico-encomenda/src/test/java/.../integration/`
  - `servico-notificacao/src/test/java/.../integration/`
  - `scripts/smoke-test-encomenda-cdc.ps1`
- Checklist tecnico:
  - [ ] revisar e documentar quais testes de integracao ja estao cobrindo transacao + outbox.
  - [ ] padronizar comando local unico para executar testes atuais.
  - [ ] registrar guia rapido de execucao em doc de qualidade.
  - [ ] validar execucao em ambiente limpo.
- Criterios de aceite:
  - `mvn test` executa com sucesso no root.
  - smoke CDC roda sem erro com stack local.
  - documentacao de execucao basica publicada.

## Fase 2 - Teste Integrado Entre Modulos (E2E Real)

### QLT-002 - Criar suite cross-module de integracao do sistema

- Tipo: Feature de qualidade
- Prioridade: Alta
- Estimativa: 3 dias
- Dependencias: QLT-001
- Evidencia esperada:
  - `tests-integracao-sistema/` (novo modulo Maven)
  - `pom.xml` (inclusao do modulo)
  - `docs/qualidade-testes-integrados.md`
- Checklist tecnico:
  - [ ] criar modulo dedicado para testes E2E com JUnit 5 e RestAssured.
  - [ ] separar testes de integracao com Failsafe (`*IT`) e nao misturar com unitarios.
  - [ ] subir dependencia de ambiente via `docker-compose.cdc.yml`.
  - [ ] implementar cenario feliz ponta a ponta:
    - cadastro de identidade/usuario;
    - emissao de token;
    - recebimento de encomenda;
    - propagacao via outbox/CDC;
    - processamento em notificacao.
  - [ ] implementar cenarios de erro criticos:
    - token sem papel correto;
    - recurso inexistente;
    - idempotencia em reprocessamento.
- Criterios de aceite:
  - suite `integration-test` passa de forma repetivel localmente.
  - pelo menos 1 fluxo cross-module completo validado automaticamente.
  - pelo menos 3 cenarios negativos relevantes cobrindo seguranca e consistencia.

## Fase 3 - Analise Estatica no Maven

### QLT-003 - Habilitar quality profile com regras estaticas

- Tipo: Chore de qualidade
- Prioridade: Alta
- Estimativa: 2 dias
- Dependencias: nenhuma
- Evidencia esperada:
  - `pom.xml` (pluginManagement e profile `quality`)
  - `config/checkstyle/` (regras de estilo)
  - `docs/qualidade-analise-estatica.md`
- Checklist tecnico:
  - [ ] configurar `maven-checkstyle-plugin`.
  - [ ] configurar `spotbugs-maven-plugin`.
  - [ ] configurar `maven-pmd-plugin`.
  - [ ] configurar `jacoco-maven-plugin` com threshold inicial.
  - [ ] configurar `org.owasp:dependency-check-maven`.
  - [ ] manter ArchUnit como regra obrigatoria de arquitetura.
  - [ ] criar comando padrao: `mvn verify -Pquality`.
- Criterios de aceite:
  - `mvn verify -Pquality` executa todas as verificacoes.
  - build falha quando regra critica e violada.
  - documentacao lista como ignorar falso positivo com justificativa versionada.

## Fase 4 - Gate de CI e Bloqueio de Merge

### QLT-004 - Criar pipeline CI com quality gate

- Tipo: Feature de plataforma
- Prioridade: Alta
- Estimativa: 2 dias
- Dependencias: QLT-002, QLT-003
- Evidencia esperada:
  - `.github/workflows/ci.yml` (ou pipeline equivalente adotado pelo time)
  - `docs/qualidade-ci-gates.md`
- Checklist tecnico:
  - [ ] etapa de build + testes unitarios.
  - [ ] etapa de testes integrados cross-module.
  - [ ] etapa de analise estatica (`-Pquality`).
  - [ ] publicacao de relatorio de testes e cobertura como artefato.
  - [ ] bloquear merge quando alguma etapa falhar.
- Criterios de aceite:
  - PR executa pipeline completo automaticamente.
  - merge fica impedido com teste/quebra de regra.
  - tempo total de pipeline dentro da janela alvo acordada pelo time.

## Fase 5 - Endurecimento de Qualidade (Roadmap)

### QLT-005 - Evoluir metas e observabilidade de qualidade

- Tipo: Melhoria continua
- Prioridade: Media
- Estimativa: 1 dia inicial + recorrente
- Dependencias: QLT-004
- Evidencia esperada:
  - `docs/qualidade-metricas.md`
  - dashboard da ferramenta de CI/qualidade adotada
- Checklist tecnico:
  - [ ] subir threshold de cobertura por modulo gradualmente.
  - [ ] monitorar tendencia de falhas e flakes.
  - [ ] criar politica de tratamento de debito tecnico (SLA por severidade).
  - [ ] registrar ADR de excecoes de regra quando necessario.
- Criterios de aceite:
  - metas de qualidade versionadas e revisadas periodicamente.
  - queda de qualidade detectada automaticamente por tendencia.
  - excecoes tecnicas registradas com prazo e responsavel.

## Ordem Recomendada de Execucao

1. QLT-001
2. QLT-002
3. QLT-003
4. QLT-004
5. QLT-005

## Definicao de Pronto (DoD) do Requisito

- existe suite de teste integrado cross-module executada automaticamente;
- principais fluxos de negocio entre modulos estao cobertos (feliz e erro);
- analise estatica roda no build e quebra em violacoes criticas;
- pipeline de CI bloqueia merge em falha de teste ou qualidade;
- documentacao operacional de execucao local e CI esta atualizada;
- regras de arquitetura continuam protegidas por ArchUnit.

## Status de Execucao (2026-02-25)

Implementado:

- modulo `tests-integracao-sistema` com fluxo cross-module automatizado;
- testes integrados com Testcontainers em `servico-encomenda` e `servico-notificacao`;
- profile `quality` no Maven com Checkstyle, PMD, SpotBugs, JaCoCo e OWASP;
- workflow CI com jobs de build, integracao com Testcontainers e analise estatica;
- documentacao de execucao local/CI em:
  - `docs/qualidade-testes-integrados.md`
  - `docs/qualidade-analise-estatica.md`
  - `docs/qualidade-ci-gates.md`

Pendente para endurecimento (fase evolutiva):

- ampliar cenarios negativos cross-module (seguranca e idempotencia);
- publicar artefatos/relatorios de teste e cobertura no CI;
- configurar required checks no provedor Git para bloqueio de merge.

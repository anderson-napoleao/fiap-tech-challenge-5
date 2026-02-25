# Backlog Tecnico - Notificacao de Moradores

## Objetivo

Implementar no `servico-notificacao` o fluxo de notificacao de chegada de encomendas com:
- consumo da mensagem de entrada `encomenda.recebida`;
- persistencia de notificacoes e historico de status por morador;
- publicacao confiavel de eventos para canal de saida via padrao Outbox;
- confirmacao de recebimento pelo morador autenticado;
- aderencia a Clean Architecture.

## Convencoes Globais

- Fluxo de status da notificacao: `PENDENTE -> ENVIADA -> CONFIRMADA` ou `PENDENTE -> FALHA`.
- `domain` e `application` sem dependencia de Spring, JPA, AMQP ou qualquer framework.
- validacoes simples de entrada no construtor do `Command` de cada `UseCase`.
- publicacao em mensageria sem XA distribuida: transacao unica para `notificacoes + outbox_event`.
- entrega de evento com semantica `at-least-once`; consumidores devem ser idempotentes por `eventId`.
- regra de integracao:
  - canal de entrada: encomendas recebidas pelo porteiro;
  - canal de saida: mensagens/eventos de notificacao aos moradores.

## Fase 1 - Runtime e Fundacao do Modulo

### NOT-001 - Habilitar runtime Spring Boot no servico-notificacao

- Modulo: `servico-notificacao`
- Entrega: modulo executavel com suporte web, seguranca, persistencia e actuator.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/pom.xml`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/ServicoNotificacaoApplication.java`, `servico-notificacao/src/main/resources/application.yml`
- Criterios de aceite:
  - `mvn -pl servico-notificacao spring-boot:run` inicia o servico;
  - endpoint `GET /actuator/health` responde com sucesso;
  - modulo continua respeitando regras de arquitetura.

### NOT-002 - Configurar seguranca JWT para endpoints de morador

- Modulo: `servico-notificacao`
- Entrega: filtro de seguranca JWT com autorizacao para area do morador.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/infrastructure/security/SecurityConfig.java`, `servico-notificacao/src/main/resources/application.yml`
- Criterios de aceite:
  - endpoints de morador exigem autenticacao;
  - requests sem token retornam `401`;
  - requests com token sem permissao retornam `403`.

## Fase 2 - Dominio e Casos de Uso

### NOT-101 - Modelar dominio de notificacao com transicoes de estado

- Modulo: `servico-notificacao`
- Entrega: entidade rica `Notificacao` e enum `StatusNotificacao`.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/domain/Notificacao.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/domain/StatusNotificacao.java`
- Criterios de aceite:
  - notificacao inicia em `PENDENTE`;
  - somente transicoes validas sao aceitas;
  - confirmacao apos `CONFIRMADA` e idempotente.

### NOT-102 - Implementar UseCase para processar encomenda recebida

- Modulo: `servico-notificacao`
- Entrega: porta de entrada + servico de aplicacao para criar notificacao a partir de `encomenda.recebida`.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/port/in/ProcessarEncomendaRecebidaUseCase.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/service/ProcessarEncomendaRecebidaService.java`
- Criterios de aceite:
  - `Command` valida campos simples no construtor;
  - `UseCase` recebe exatamente um parametro `Command`;
  - camada `application` depende apenas de `domain` e portas.

### NOT-103 - Implementar UseCase de confirmacao de notificacao

- Modulo: `servico-notificacao`
- Entrega: caso de uso para confirmar recebimento de notificacao por morador autenticado.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/port/in/ConfirmarRecebimentoNotificacaoUseCase.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/service/ConfirmarRecebimentoNotificacaoService.java`
- Criterios de aceite:
  - `Command` valida `notificacaoId` e `moradorId`;
  - confirmacao valida ownership da notificacao;
  - operacao e idempotente para notificacao ja confirmada.

## Fase 3 - Persistencia e Outbox

### NOT-201 - Persistir notificacao e outbox na mesma transacao

- Modulo: `servico-notificacao`
- Entrega: adaptador transacional que grava `notificacoes` e `outbox_event` atomicamente.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/port/out/RegistrarNotificacaoComOutboxPort.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/adapter/out/RegistrarNotificacaoComOutboxAdapter.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/infrastructure/persistence/entity/NotificacaoEntity.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/infrastructure/persistence/entity/OutboxEventEntity.java`
- Criterios de aceite:
  - em sucesso, existem registros consistentes em `notificacoes` e `outbox_event`;
  - em falha transacional, nenhum dos dois registros e confirmado;
  - `UseCase` nao publica direto em broker.

### NOT-202 - Criar migracoes de banco para notificacao e outbox

- Modulo: `servico-notificacao`
- Entrega: esquema de banco com tabelas, constraints e indices para consulta e CDC.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/resources/db/migration/V1__create_notificacoes_table.sql`, `servico-notificacao/src/main/resources/db/migration/V2__create_outbox_event_table.sql`
- Criterios de aceite:
  - tabela `notificacoes` contem `morador_id`, `status`, `canal`, `destino`;
  - `source_event_id` possui restricao de unicidade para idempotencia;
  - existe indice para consulta `morador_id + status + created_at`.

### NOT-203 - Definir contratos de eventos de notificacao

- Modulo: `servico-notificacao`
- Entrega: payloads versionados para eventos de saida do modulo.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/event/NotificacaoSolicitadaEvent.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/event/NotificacaoConfirmadaEvent.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/adapter/out/OutboxPayloadMapper.java`
- Criterios de aceite:
  - evento inclui `eventId`, `eventVersion`, `occurredAt`, `correlationId`, `moradorId`, `encomendaId`, `status`;
  - contratos contemplam `notificacao.solicitada`, `notificacao.enviada`, `notificacao.falhou`, `notificacao.confirmada`;
  - payload pronto para publicacao via pipeline de outbox.

### NOT-204 - Provisionar pipeline de mensageria (entrada e saida)

- Modulo: raiz e ambiente local
- Entrega: configuracao operacional para consumir entrada e publicar saida a partir do outbox.
- Status: planejado em 2026-02-25
- Evidencia: `docker-compose.yml`, `infra/debezium/`, `DOCKER.md`
- Criterios de aceite:
  - evento de entrada `encomenda.recebida` chega ao `servico-notificacao`;
  - eventos de saida de notificacao chegam ao topico/fila alvo;
  - operacao documenta comportamento `at-least-once` e idempotencia.

## Fase 4 - API de Morador (Listagem e Confirmacao)

### NOT-301 - Expor endpoint para listar notificacoes nao confirmadas do usuario logado

- Modulo: `servico-notificacao`
- Entrega: API `GET /morador/notificacoes?confirmada=false` com paginacao.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/adapter/in/web/MoradorNotificacaoController.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/port/in/ListarNotificacoesPendentesUseCase.java`
- Criterios de aceite:
  - endpoint retorna somente notificacoes do morador autenticado;
  - filtro de nao confirmadas funciona sem receber `moradorId` por parametro;
  - resposta suporta paginacao (`page`, `size`) e ordenacao por data.

### NOT-302 - Expor endpoint para confirmar notificacao selecionada

- Modulo: `servico-notificacao`
- Entrega: API `POST /morador/notificacoes/{id}/confirmacao`.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/adapter/in/web/MoradorNotificacaoController.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/port/in/ConfirmarRecebimentoNotificacaoUseCase.java`
- Criterios de aceite:
  - endpoint confirma apenas notificacao do morador autenticado;
  - tentativa de confirmar notificacao de outro morador retorna `403` ou `404`;
  - confirmacao repetida e idempotente.

## Fase 5 - Qualidade e Governanca Arquitetural

### NOT-401 - Cobertura de testes de contrato e validacao de use case

- Modulo: `servico-notificacao`
- Entrega: testes de contrato de use case e validacoes de command.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/test/java/br/com/condominio/servico/notificacao/architecture/UseCaseContractTest.java`, `servico-notificacao/src/test/java/br/com/condominio/servico/notificacao/application/port/in/UseCaseCommandValidationTest.java`
- Criterios de aceite:
  - interfaces de use case recebem apenas `Command`;
  - construtores de `Command` rejeitam campos invalidos;
  - cobertura inclui casos obrigatorios de erro e sucesso.

### NOT-402 - Testes de API para listagem e confirmacao do morador

- Modulo: `servico-notificacao`
- Entrega: testes de controller para cenarios de autenticacao, autorizacao, listagem e confirmacao.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/test/java/br/com/condominio/servico/notificacao/adapter/in/web/MoradorNotificacaoControllerTest.java`
- Criterios de aceite:
  - request sem token retorna `401`;
  - listagem retorna somente notificacoes nao confirmadas do usuario logado;
  - confirmacao valida retorna sucesso e atualiza estado.

### NOT-403 - Teste de integracao da atomicidade notificacao + outbox

- Modulo: `servico-notificacao`
- Entrega: teste de integracao cobrindo consistencia transacional e idempotencia.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/test/java/br/com/condominio/servico/notificacao/integration/ProcessarNotificacaoOutboxIntegrationTest.java`
- Criterios de aceite:
  - caminho de sucesso persiste `notificacao` e `outbox_event`;
  - caminho de erro faz rollback completo;
  - reprocessamento de `sourceEventId` nao duplica notificacao.

### NOT-404 - Preservar regras de arquitetura do modulo

- Modulo: `servico-notificacao`
- Entrega: manutencao e evolucao dos testes de arquitetura.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/test/java/br/com/condominio/servico/notificacao/architecture/CleanArchitectureTest.java`
- Criterios de aceite:
  - `domain` permanece puro;
  - `application` sem dependencias de framework;
  - build falha se houver quebra de fronteira arquitetural.

## Fase 6 - API GraphQL de Historico (Sem NoSQL)

### NOT-501 - Habilitar runtime GraphQL no servico-notificacao

- Modulo: `servico-notificacao`
- Entrega: suporte GraphQL no modulo para consultas de historico por morador.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/pom.xml`, `servico-notificacao/src/main/resources/application.yml`, `servico-notificacao/src/main/resources/graphql/`
- Criterios de aceite:
  - endpoint `POST /graphql` responde consultas validas;
  - servico continua subindo com `actuator/health` e seguranca ativa;
  - camada `application` permanece sem dependencia de framework.

### NOT-502 - Implementar UseCase de consulta de historico de notificacoes

- Modulo: `servico-notificacao`
- Entrega: porta de entrada + servico de aplicacao para consultar historico por morador autenticado.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/port/in/ConsultarHistoricoNotificacoesUseCase.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/service/ConsultarHistoricoNotificacoesService.java`
- Criterios de aceite:
  - `Command` valida `moradorId`, paginacao e filtros simples no construtor;
  - `UseCase` recebe exatamente um parametro `Command`;
  - retorno inclui estado atual e timestamps (`criadaEm`, `enviadaEm`, `falhaEm`, `confirmadaEm`).

### NOT-503 - Implementar porta de consulta e adaptador de persistencia para historico

- Modulo: `servico-notificacao`
- Entrega: consulta paginada por morador, com filtro opcional por `encomendaId`.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/port/out/NotificacaoHistoricoRepositoryPort.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/adapter/out/NotificacaoHistoricoRepositoryAdapter.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/infrastructure/persistence/repository/SpringDataNotificacaoRepository.java`
- Criterios de aceite:
  - consulta retorna somente notificacoes do morador informado;
  - ordenacao por data de criacao decrescente;
  - paginacao suporta `page` e `size`.

### NOT-504 - Expor query GraphQL para historico de notificacoes do morador

- Modulo: `servico-notificacao`
- Entrega: query GraphQL `historicoNotificacoes` na camada de entrada.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/adapter/in/graphql/`, `servico-notificacao/src/main/resources/graphql/notificacao-historico.graphqls`
- Criterios de aceite:
  - query usa `moradorId` derivado do token JWT autenticado;
  - resposta contem `id`, `encomendaId`, `status`, `criadaEm`, `enviadaEm`, `falhaEm`, `confirmadaEm`, `motivoFalha`;
  - filtro por `encomendaId` e paginacao disponiveis.

### NOT-505 - Garantir seguranca de autorizacao no endpoint GraphQL

- Modulo: `servico-notificacao`
- Entrega: regra de seguranca para `/graphql` com escopo de morador.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/infrastructure/security/SecurityConfig.java`
- Criterios de aceite:
  - request sem token retorna `401`;
  - request autenticada sem `ROLE_MORADOR` retorna `403`;
  - request com `ROLE_MORADOR` acessa somente dados do proprio morador.

### NOT-506 - Cobertura de testes de GraphQL (contrato, seguranca e consulta)

- Modulo: `servico-notificacao`
- Entrega: testes para query GraphQL de historico, validacoes e autorizacao.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/test/java/br/com/condominio/servico/notificacao/adapter/in/graphql/`, `servico-notificacao/src/test/java/br/com/condominio/servico/notificacao/application/port/in/UseCaseCommandValidationTest.java`, `servico-notificacao/src/test/java/br/com/condominio/servico/notificacao/architecture/UseCaseContractTest.java`
- Criterios de aceite:
  - consulta valida retorna dados esperados;
  - erros de validacao retornam falha adequada;
  - cenarios `401` e `403` cobertos por testes.

## Fase 7 - Evolucao de Historico Completo (Roadmap Futuro)

### NOT-601 - Criar trilha append-only de mudancas de status em SQL

- Modulo: `servico-notificacao`
- Entrega: tabela historica de transicoes de status (`notificacao_status_historico`).
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/resources/db/migration/`
- Criterios de aceite:
  - cada mudanca de estado gera novo registro historico imutavel;
  - registros contem `notificacaoId`, `status`, `ocorridoEm`, `motivo` e metadados de correlacao;
  - indice para consulta por `notificacaoId` e por `moradorId + ocorridoEm`.

### NOT-602 - Registrar historico de transicoes na mesma transacao da notificacao

- Modulo: `servico-notificacao`
- Entrega: persistencia atomica de `notificacoes + historico_status`.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/adapter/out/`, `servico-notificacao/src/test/java/br/com/condominio/servico/notificacao/integration/`
- Criterios de aceite:
  - confirmacao, envio e falha gravam eventos de historico;
  - em falha transacional nao persiste notificacao sem historico;
  - operacoes idempotentes nao duplicam transicoes invalidas.

### NOT-603 - Evoluir query GraphQL para timeline completa por notificacao/encomenda

- Modulo: `servico-notificacao`
- Entrega: retorno de timeline completa de transicoes, nao apenas snapshot atual.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/resources/graphql/notificacao-historico.graphqls`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/adapter/in/graphql/`
- Criterios de aceite:
  - query retorna sequencia ordenada de transicoes por notificacao;
  - suporte a agregacao por `encomendaId`;
  - compatibilidade retroativa da query principal.

### NOT-604 - Publicar eventos de mudanca de status via outbox (opcional)

- Modulo: `servico-notificacao`
- Entrega: eventos `notificacao.enviada`, `notificacao.falhou`, `notificacao.confirmada` no outbox.
- Status: planejado em 2026-02-25
- Evidencia: `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/adapter/out/RegistrarNotificacaoComOutboxAdapter.java`, `servico-notificacao/src/main/java/br/com/condominio/servico/notificacao/application/event/`
- Criterios de aceite:
  - contratos versionados e payload consistente com correlacao;
  - publicacao na mesma semantica `at-least-once`;
  - consumidores documentados com requisito de idempotencia.

### NOT-605 - Avaliar read model NoSQL para historico (opcional por escala)

- Modulo: arquitetura da solucao
- Entrega: decisao tecnica documentada para manter SQL ou adicionar NoSQL para leitura.
- Status: planejado em 2026-02-25
- Evidencia: `docs/notificacao-arquitetura-historico.md` (ou ADR equivalente)
- Criterios de aceite:
  - decisao considera volume, latencia, custo operacional e consistencia eventual;
  - plano de migracao definido quando NoSQL for adotado;
  - sem regressao de seguranca/autorizacao na consulta de historico.

## Ordem Recomendada de Execucao

1. NOT-001
2. NOT-002
3. NOT-101
4. NOT-102
5. NOT-201
6. NOT-202
7. NOT-203
8. NOT-204
9. NOT-301
10. NOT-103
11. NOT-302
12. NOT-401
13. NOT-402
14. NOT-403
15. NOT-404
16. NOT-501
17. NOT-502
18. NOT-503
19. NOT-504
20. NOT-505
21. NOT-506
22. NOT-601
23. NOT-602
24. NOT-603
25. NOT-604
26. NOT-605

## Definicao de Pronto (DoD)

- build Maven verde no `servico-notificacao`;
- testes unitarios, de API, integracao transacional e arquitetura passando;
- consumo do canal de entrada funcionando com idempotencia;
- notificacao persistida com status e historico de tentativas;
- eventos de saida publicados via outbox para canal de saida;
- listagem de nao confirmadas do morador autenticado funcionando;
- confirmacao de notificacao funcionando de forma idempotente;
- fluxo operacional de mensageria documentado;
- query GraphQL de historico do morador autenticado funcionando (fase sem NoSQL);
- roadmap de historico completo (append-only e opcao NoSQL) documentado.

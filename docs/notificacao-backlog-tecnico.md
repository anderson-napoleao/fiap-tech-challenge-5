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
- Evidencia: `infra/docker/docker-compose.cdc.yml`, `infra/debezium/`, `docs/notificacao-operacao-outbox-cdc.md`
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

## Definicao de Pronto (DoD)

- build Maven verde no `servico-notificacao`;
- testes unitarios, de API, integracao transacional e arquitetura passando;
- consumo do canal de entrada funcionando com idempotencia;
- notificacao persistida com status e historico de tentativas;
- eventos de saida publicados via outbox para canal de saida;
- listagem de nao confirmadas do morador autenticado funcionando;
- confirmacao de notificacao funcionando de forma idempotente;
- fluxo operacional de mensageria documentado.

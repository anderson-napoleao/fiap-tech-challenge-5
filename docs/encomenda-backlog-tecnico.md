# Backlog Tecnico - Recebimento de Encomendas

## Objetivo

Implementar no `servico-encomenda` o fluxo de recebimento de encomendas pela portaria com:
- acesso restrito a `FUNCIONARIO`;
- persistencia da encomenda com status inicial `RECEBIDA`;
- publicacao confiavel de evento para fila via padrao Outbox CDC;
- aderencia a Clean Architecture.

## Convencoes Globais

- Fluxo de status da encomenda: `RECEBIDA -> RETIRADA`.
- `domain` e `application` sem dependencia de Spring, JPA, AMQP ou qualquer framework.
- validacoes simples de entrada no construtor do `Command` de cada `UseCase`.
- publicacao em fila sem XA distribuida: transacao unica para `encomendas + outbox_event`.
- entrega de evento com semantica `at-least-once`; consumidor deve ser idempotente por `eventId`.

## Fase 1 - Runtime e Fundacao do Modulo

### ENC-001 - Habilitar runtime Spring Boot no servico-encomenda

- Modulo: `servico-encomenda`
- Entrega: modulo executavel com suporte web, seguranca, persistencia e actuator.
- Status: planejado em 2026-02-21
- Evidencia: `servico-encomenda/pom.xml`, `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/ServicoEncomendaApplication.java`, `servico-encomenda/src/main/resources/application.yml`
- Criterios de aceite:
  - `mvn -pl servico-encomenda spring-boot:run` inicia o servico;
  - endpoint `GET /actuator/health` responde com sucesso;
  - modulo continua respeitando regras de arquitetura.

### ENC-002 - Configurar base de seguranca JWT para API de portaria

- Modulo: `servico-encomenda`
- Entrega: filtro de seguranca JWT com regra de autorizacao por papel.
- Status: planejado em 2026-02-21
- Evidencia: `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/infrastructure/security/SecurityConfig.java`, `servico-encomenda/src/main/resources/application.yml`
- Criterios de aceite:
  - endpoint de recebimento exige autenticacao;
  - somente token com `ROLE_FUNCIONARIO` acessa a funcionalidade;
  - requests sem token retornam `401` e com papel invalido retornam `403`.

## Fase 2 - Dominio e Caso de Uso de Recebimento

### ENC-101 - Modelar dominio de encomenda com transicao `RECEBIDA -> RETIRADA`

- Modulo: `servico-encomenda`
- Entrega: entidade rica com regras de negocio e status do ciclo de vida.
- Status: planejado em 2026-02-21
- Evidencia: `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/domain/Encomenda.java`, `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/domain/StatusEncomenda.java`
- Criterios de aceite:
  - criacao de encomenda define status inicial `RECEBIDA`;
  - transicao para `RETIRADA` valida pre-condicoes e impede regressao de status;
  - campos obrigatorios de negocio: `nomeDestinatario`, `apartamento`, `bloco`, `descricao`.

### ENC-102 - Implementar UseCase de recebimento de encomenda

- Modulo: `servico-encomenda`
- Entrega: porta de entrada + servico de aplicacao para registrar recebimento.
- Status: planejado em 2026-02-21
- Evidencia: `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/application/port/in/ReceberEncomendaUseCase.java`, `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/application/service/ReceberEncomendaService.java`
- Criterios de aceite:
  - `Command` valida campos simples no construtor;
  - `UseCase` recebe exatamente um parametro `Command`;
  - camada `application` depende apenas de `domain` e portas.

### ENC-103 - Expor endpoint de recebimento para portaria

- Modulo: `servico-encomenda`
- Entrega: API `POST /portaria/encomendas` com request/response padronizados.
- Status: planejado em 2026-02-21
- Evidencia: `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/adapter/in/web/PortariaEncomendaController.java`, `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/adapter/in/web/dto/ReceberEncomendaRequest.java`, `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/adapter/in/web/dto/EncomendaResponse.java`
- Criterios de aceite:
  - endpoint retorna `201` ao receber encomenda valida;
  - resposta inclui `id`, `status` e dados relevantes da encomenda;
  - endpoint bloqueia acesso fora de `ROLE_FUNCIONARIO`.

## Fase 3 - Persistencia e Outbox CDC

### ENC-201 - Persistir encomenda e evento outbox na mesma transacao

- Modulo: `servico-encomenda`
- Entrega: adaptador transacional que grava `encomendas` e `outbox_event` atomicamente.
- Status: planejado em 2026-02-21
- Evidencia: `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/application/port/out/RegistrarRecebimentoComOutboxPort.java`, `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/adapter/out/RegistrarRecebimentoComOutboxAdapter.java`, `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/infrastructure/persistence/entity/EncomendaEntity.java`, `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/infrastructure/persistence/entity/OutboxEventEntity.java`
- Criterios de aceite:
  - em sucesso, existem registros consistentes em `encomendas` e `outbox_event`;
  - em falha transacional, nenhum dos dois registros e confirmado;
  - `UseCase` nao faz publish direto no RabbitMQ.

### ENC-202 - Criar migracoes de banco para encomenda e outbox

- Modulo: `servico-encomenda`
- Entrega: esquema de banco com tabelas, constraints e indices para CDC.
- Status: planejado em 2026-02-21
- Evidencia: `servico-encomenda/src/main/resources/db/migration/V1__create_encomendas_table.sql`, `servico-encomenda/src/main/resources/db/migration/V2__create_outbox_event_table.sql`
- Criterios de aceite:
  - tabela `encomendas` contem `bloco` e `status`;
  - tabela `outbox_event` contem identificador unico de evento e payload;
  - indices suportam leitura eficiente do conector CDC.

### ENC-203 - Definir contrato de evento `EncomendaRecebida` para RabbitMQ

- Modulo: `servico-encomenda`
- Entrega: payload versionado registrado no outbox para consumo externo.
- Status: planejado em 2026-02-21
- Evidencia: `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/application/event/EncomendaRecebidaEvent.java`, `servico-encomenda/src/main/java/br/com/condominio/servico/encomenda/adapter/out/OutboxPayloadMapper.java`
- Criterios de aceite:
  - evento inclui `eventId`, `eventVersion`, `occurredAt`, `encomendaId`, `nomeDestinatario`, `apartamento`, `bloco`, `descricao`, `recebidoPor`, `status`;
  - `status` publicado no recebimento e `RECEBIDA`;
  - payload pronto para publicacao via Debezium Outbox para RabbitMQ.

### ENC-204 - Provisionar pipeline CDC (Outbox -> RabbitMQ)

- Modulo: raiz e ambiente local
- Entrega: configuracao operacional de Debezium para captura do outbox e entrega na fila.
- Status: planejado em 2026-02-21
- Evidencia: `docs/encomenda-operacao-outbox-cdc.md`, `infra/docker/docker-compose.cdc.yml`, `infra/debezium/encomenda-outbox-connector.json`
- Criterios de aceite:
  - conector CDC captura novos registros da tabela `outbox_event`;
  - evento `EncomendaRecebida` chega na exchange/queue alvo do RabbitMQ;
  - operacao documenta comportamento `at-least-once` e idempotencia esperada no consumidor.

## Fase 4 - Qualidade e Governanca Arquitetural

### ENC-301 - Cobertura de testes de contrato e validacao de use case

- Modulo: `servico-encomenda`
- Entrega: testes de contrato de use case e validacoes de command.
- Status: planejado em 2026-02-21
- Evidencia: `servico-encomenda/src/test/java/br/com/condominio/servico/encomenda/architecture/UseCaseContractTest.java`, `servico-encomenda/src/test/java/br/com/condominio/servico/encomenda/application/port/in/UseCaseCommandValidationTest.java`
- Criterios de aceite:
  - interfaces de use case recebem apenas `Command`;
  - construtores de `Command` rejeitam campos invalidos;
  - cobertura inclui casos obrigatorios de erro e sucesso.

### ENC-302 - Testes de seguranca e API de recebimento

- Modulo: `servico-encomenda`
- Entrega: testes de controller para cenarios de autenticacao/autorizacao e sucesso.
- Status: planejado em 2026-02-21
- Evidencia: `servico-encomenda/src/test/java/br/com/condominio/servico/encomenda/adapter/in/web/PortariaEncomendaControllerTest.java`
- Criterios de aceite:
  - request sem token retorna `401`;
  - request autenticada sem `ROLE_FUNCIONARIO` retorna `403`;
  - request com `ROLE_FUNCIONARIO` valida retorna `201`.

### ENC-303 - Teste de integracao da atomicidade `encomenda + outbox`

- Modulo: `servico-encomenda`
- Entrega: teste de integracao cobrindo consistencia transacional.
- Status: planejado em 2026-02-21
- Evidencia: `servico-encomenda/src/test/java/br/com/condominio/servico/encomenda/integration/ReceberEncomendaOutboxIntegrationTest.java`
- Criterios de aceite:
  - caminho de sucesso persiste `encomenda` e `outbox_event`;
  - caminho de erro faz rollback completo;
  - nao existe cenario com encomenda persistida sem evento correspondente.

### ENC-304 - Preservar regras de arquitetura do modulo

- Modulo: `servico-encomenda`
- Entrega: manutencao e evolucao dos testes de arquitetura.
- Status: planejado em 2026-02-21
- Evidencia: `servico-encomenda/src/test/java/br/com/condominio/servico/encomenda/architecture/CleanArchitectureTest.java`
- Criterios de aceite:
  - `domain` permanece puro;
  - `application` sem dependencias de framework;
  - build falha se houver quebra de fronteira arquitetural.

## Ordem Recomendada de Execucao

1. ENC-001
2. ENC-002
3. ENC-101
4. ENC-102
5. ENC-103
6. ENC-201
7. ENC-202
8. ENC-203
9. ENC-204
10. ENC-301
11. ENC-302
12. ENC-303
13. ENC-304

## Definicao de Pronto (DoD)

- build Maven verde no `servico-encomenda`;
- testes unitarios, de API, integracao transacional e arquitetura passando;
- endpoint de recebimento protegido por `ROLE_FUNCIONARIO`;
- encomenda criada com status `RECEBIDA` e transicao para `RETIRADA` validada;
- evento `EncomendaRecebida` com `bloco` persistido em outbox na mesma transacao da encomenda;
- fluxo de CDC para RabbitMQ validado em ambiente local com documentacao operacional.

# Operacao Outbox CDC - servico-notificacao

## Objetivo

Publicar eventos de `outbox_event` do `servico-notificacao` para o canal de saida de notificacao
com semantica `at-least-once`.

## Arquivos de referencia

- `infra/docker/docker-compose.cdc.yml`
- `infra/debezium/notificacao-outbox-connector.json`
- `docs/notificacao-backlog-tecnico.md`

## Subir stack CDC local

No diretorio raiz do repositorio:

```powershell
docker compose -f infra/docker/docker-compose.cdc.yml up -d
```

## Subir servico-notificacao

```powershell
mvn -pl servico-notificacao spring-boot:run "-Dspring-boot.run.profiles=cdc"
```

## Registrar connector de outbox (source)

```powershell
curl -X POST "http://localhost:8084/connectors" `
  -H "Content-Type: application/json" `
  --data-binary "@infra/debezium/notificacao-outbox-connector.json"
```

Status do connector:

```powershell
curl "http://localhost:8084/connectors/notificacao-outbox-connector/status"
```

## Validacao rapida

1. Gerar uma notificacao no `servico-notificacao` que persista em `notificacoes` e `outbox_event`.
2. Validar linha criada em `outbox_event`.
3. Validar mensagem no topico Kafka `notificacao.evento`.

Banco dedicado do `servico-notificacao`:
- host local: `localhost:5433`
- database: `condominio_notificacao`
- em Adminer (`http://localhost:8085`), use servidor `postgres-notificacao`.

## Observacoes de confiabilidade

- Atomicidade: `notificacoes` e `outbox_event` sao gravadas na mesma transacao do banco.
- Entrega: outbox + CDC operam em `at-least-once`.
- Consumidor do canal de saida deve aplicar idempotencia por `eventId`.

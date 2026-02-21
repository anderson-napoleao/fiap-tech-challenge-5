# Operacao Outbox CDC - servico-encomenda

## Objetivo

Publicar eventos de `outbox_event` do `servico-encomenda` para RabbitMQ com semantica `at-least-once`.

## Arquivos de referencia

- `infra/docker/docker-compose.cdc.yml`
- `infra/debezium/encomenda-outbox-connector.json`
- `infra/debezium/encomenda-rabbit-sink-connector.json`

## Subir stack CDC local

No diretorio raiz do repositorio:

```powershell
docker compose -f infra/docker/docker-compose.cdc.yml up -d
```

## Subir servico-encomenda com Postgres (perfil CDC)

```powershell
mvn -pl servico-encomenda spring-boot:run -Dspring-boot.run.profiles=cdc
```

## Registrar conectores no Kafka Connect

### 1) Source connector (Postgres Outbox -> Kafka)

```powershell
curl -X POST "http://localhost:8084/connectors" `
  -H "Content-Type: application/json" `
  --data-binary "@infra/debezium/encomenda-outbox-connector.json"
```

### 2) Sink connector (Kafka -> RabbitMQ)

```powershell
curl -X POST "http://localhost:8084/connectors" `
  -H "Content-Type: application/json" `
  --data-binary "@infra/debezium/encomenda-rabbit-sink-connector.json"
```

## Validacao rapida

1. Registrar uma encomenda em `POST /portaria/encomendas`.
2. Validar linha criada em `outbox_event`.
3. Validar mensagem na fila RabbitMQ (`encomenda.recebida.queue`) pelo painel `http://localhost:15672`.

## Observacoes de confiabilidade

- Atomicidade: `encomendas` e `outbox_event` sao gravadas na mesma transacao do banco.
- Entrega: CDC e sink operam em `at-least-once`.
- Consumidor: deve aplicar idempotencia usando `eventId`.
- O sink RabbitMQ exige plugin Kafka Connect compativel instalado no container `connect`.

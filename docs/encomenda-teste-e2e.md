# Teste E2E - Usuario FUNCIONARIO e Recebimento de Encomenda

## Escopo

Fluxo completo para validar o que foi implementado ate a fase 4:
- cria usuario `FUNCIONARIO` na identidade (referencia: Lucas Amaral);
- gera token JWT desse funcionario;
- registra recebimento de encomenda na API de portaria;
- valida visualmente tabelas e eventos.

## Pre-requisitos

- Java 21 e Maven 3.9+
- Docker Desktop
- Postman

## 1) Subir stack CDC e ferramentas visuais

```powershell
docker compose -f infra/docker/docker-compose.cdc.yml up -d
```

UIs disponiveis:
- PostgreSQL (Adminer): `http://localhost:8085`
- RabbitMQ Management: `http://localhost:15672` (`guest` / `guest`)
- Kafka UI: `http://localhost:8086`

## 2) Subir servicos

Terminal 1:

```powershell
mvn -pl servico-identidade spring-boot:run
```

Terminal 2:

```powershell
mvn -pl servico-encomenda spring-boot:run "-Dspring-boot.run.profiles=cdc"
```

## 3) Registrar connector de outbox (CDC)

```powershell
curl -X POST "http://localhost:8084/connectors" `
  -H "Content-Type: application/json" `
  --data-binary "@infra/debezium/encomenda-outbox-connector.json"
```

Status do connector:

```powershell
curl "http://localhost:8084/connectors/encomenda-outbox-connector/status"
```

## 4) Rodar collection no Postman

Importe:
- `docs/postman/encomenda-e2e.postman_collection.json`

Execute a collection inteira. Ela faz:
1. Health check dos servicos.
2. Cria usuario `FUNCIONARIO` com email dinamico de `Lucas Amaral`.
3. Gera token JWT.
4. Registra recebimento de encomenda.

## 5) Verificacao visual das tabelas

Abra `http://localhost:8085` e conecte:
- System: `PostgreSQL`
- Server: `postgres`
- Username: `postgres`
- Password: `postgres`
- Database: `condominio`

SQL para `encomendas`:

```sql
SELECT id, nome_destinatario, apartamento, bloco, recebido_por, status, data_recebimento
FROM encomendas
ORDER BY id DESC;
```

SQL para `outbox_event`:

```sql
SELECT id, aggregatetype, aggregateid, type, event_version, event_timestamp, event_timestamp_ms
FROM outbox_event
ORDER BY event_timestamp DESC;
```

Para ver payload do evento:

```sql
SELECT id, payload
FROM outbox_event
ORDER BY event_timestamp DESC
LIMIT 5;
```

## 6) Verificacao visual dos eventos

No Kafka UI (`http://localhost:8086`):
1. abra o cluster `condominio`;
2. abra o topico `encomenda.recebida`;
3. confira mensagens com `eventId`, `encomendaId`, `bloco`, `status`.

No RabbitMQ (`http://localhost:15672`):
- so havera mensagem se o sink connector RabbitMQ estiver instalado e configurado no Kafka Connect.

## Observacao importante

No `servico-identidade`, o usuario possui email/role, nao ha campo de nome.  
Na collection, `Lucas Amaral` e mantido como referencia do cenario, e o usuario real e identificado pelo email dinamico.

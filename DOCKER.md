# Docker Compose - Sistema Condominio

Este documento descreve a operacao do ambiente local com Docker Compose, sem scripts auxiliares.

## Servicos

### Aplicacao
- `frontend` (`3000`): interface web React
- `servico-identidade` (`8081`): autenticacao e emissao de JWT
- `servico-usuario` (`8082`): cadastro de usuarios e moradores
- `servico-encomenda` (`8083`): recebimento, listagem e retirada de encomendas
- `servico-notificacao` (`8084`): notificacoes para moradores

### Dados e mensageria
- `postgres-identidade` (`5431`)
- `postgres-usuario` (`5434`)
- `postgres-encomenda` (`5432`)
- `postgres-notificacao` (`5433`)
- `zookeeper` (`2181`)
- `kafka` (`9092`)
- `kafka-connect` (`8085`)
- `cdc-init`: registra automaticamente os connectors Debezium ao subir o ambiente

### Observabilidade e apoio
- `elasticsearch` (`9200`)
- `logstash` (`5044`)
- `kibana` (`5601`)
- `filebeat`
- `kafka-ui` (`8086`)
- `adminer` (`8087`)
- `docs-site` (`8090`)

## Como subir

No diretorio raiz:

```bash
docker compose up -d --build
```

Verificar status:

```bash
docker compose ps
```

Ver logs:

```bash
docker compose logs -f
docker compose logs -f servico-encomenda
docker compose logs -f cdc-init
```

Parar ambiente:

```bash
docker compose down
```

Parar e remover volumes:

```bash
docker compose down -v --remove-orphans
```

## CDC e Connectors

O servico `cdc-init` aguarda o Kafka Connect ficar disponivel e aplica os conectores:
- `encomenda-outbox-connector`
- `notificacao-outbox-connector`

Arquivos usados:
- `infra/debezium/encomenda-outbox-connector.config.json`
- `infra/debezium/notificacao-outbox-connector.config.json`

Validacao:

```bash
docker compose logs -f cdc-init
curl http://localhost:8085/connectors
curl http://localhost:8085/connectors/encomenda-outbox-connector/status
curl http://localhost:8085/connectors/notificacao-outbox-connector/status
```

## Enderecos uteis

- Frontend: `http://localhost:3000`
- Swagger Identidade: `http://localhost:8081/swagger-ui/index.html`
- Swagger Usuario: `http://localhost:8082/swagger-ui/index.html`
- Swagger Encomenda: `http://localhost:8083/swagger-ui/index.html`
- Swagger Notificacao: `http://localhost:8084/swagger-ui/index.html`
- Kafka UI: `http://localhost:8086`
- Adminer: `http://localhost:8087`
- Kibana: `http://localhost:5601`
- Portal de documentacao: `http://localhost:8090`

## Troubleshooting rapido

- Se um servico falhar no boot:
```bash
docker compose logs -f <servico>
```

- Rebuild de um servico:
```bash
docker compose up -d --build <servico>
```

- Limpeza completa do ambiente local:
```bash
docker compose down -v --remove-orphans
docker system prune -f
```

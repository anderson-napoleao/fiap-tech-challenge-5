# Docker Compose - Sistema Condomínio

## 🏗️ Arquitetura

### Serviços da Aplicação
- **servico-identidade** (8081): Gestão de identidade e autenticação
- **servico-usuario** (8082): Gestão de usuários e moradores
- **servico-encomenda** (8083): Gestão de encomendas
- **servico-notificacao** (8084): Sistema de notificações

### Infraestrutura de Dados
- **postgres-identidade** (5431): Banco do serviço de identidade
- **postgres-usuario** (5434): Banco do serviço de usuários
- **postgres-encomenda** (5432): Banco do serviço de encomendas
- **postgres-notificacao** (5433): Banco do serviço de notificações

### Infraestrutura de Mensageria
- **zookeeper** (2181): Coordenação Kafka
- **kafka** (9092): Broker de eventos
- **kafka-connect** (8085): Conectores CDC

### Stack ELK (Logs e Tracing)
- **elasticsearch** (9200): Armazenamento de logs
- **logstash** (5044): Processamento de logs
- **kibana** (5601): Visualização de logs
- **filebeat**: Coleta de logs dos containers

### Interfaces de Visualização
- **kafka-ui** (8086): Interface Kafka
- **adminer** (8087): Interface bancos de dados

## 🚀 Comandos

### Iniciar Sistema Completo
```bash
# PowerShell
./start.ps1

# Bash/Linux
./start.sh
```

### Gerenciamento
```bash
# Apenas build das imagens
./start.ps1 -Build

# Ver status dos containers
./start.ps1 -Status

# Ver logs (todos ou serviço específico)
./start.ps1 -Logs
./start.ps1 -Logs -Service servico-usuario

# Parar sistema completo
./start.ps1 -Down
```

### Comandos Docker
```bash
# Subir tudo
docker-compose up -d

# Ver logs
docker-compose logs -f [serviço]

# Parar tudo
docker-compose down --volumes --remove-orphans

# Rebuild específico
docker-compose up -d --build servico-usuario
```

## 📊 Acesso às Interfaces

### Logs e Monitoramento
- **Kibana**: http://localhost:5601
  - Index pattern: `condominio-logs-*`
  - Campos disponíveis: service, log_level, trace_id, span_id

### Mensageria
- **Kafka UI**: http://localhost:8086
  - Tópicos: `encomenda.recebida`, etc.
  - Connectors: Debezium CDC

### Bancos de Dados
- **Adminer**: http://localhost:8087
  - PostgreSQL hosts:
    - Identidade: postgres-identidade:5432
    - Usuário: postgres-usuario:5432
    - Encomenda: postgres-encomenda:5432
    - Notificação: postgres-notificacao:5432

## 🔧 Configuração de Logs

### Formato de Logging
Os serviços usam structured logging com JSON:
```json
{
  "timestamp": "2024-02-25T00:30:00.000Z",
  "level": "INFO",
  "thread": "http-nio-8082-exec-1",
  "logger": "br.com.condominio.servico.usuario",
  "message": "Usuário criado com sucesso",
  "traceId": "abc123",
  "spanId": "def456",
  "service": "servico-usuario"
}
```

### Kibana Dashboards
1. **Logs por Serviço**: Filtrar por campo `service`
2. **Distribuição de Erros**: Filtrar `log_level: ERROR`
3. **Tracing Distribuído**: Agrupar por `trace_id`
4. **Performance**: Métricas de tempo por operação

## 🏥 Health Checks

Todos os serviços expõem health checks:
- `GET /actuator/health`
- Docker health checks configurados
- Status disponível em `docker-compose ps`

## 🔒 Segurança

- Non-root users nos containers
- Senhas padrão apenas para desenvolvimento
- Volumes persistentes para dados
- Network isolation via bridge network

## 📈 Performance

- Java container memory: 75% do container
- Health checks a cada 30s
- Logs rotativos (10MB, 30 dias)
- Elasticsearch com 512MB RAM (desenvolvimento)

## 🐛 Troubleshooting

### Problemas Comuns
1. **Portas em uso**: Verificar se portas estão livres
2. **Memória insuficiente**: Aumentar RAM do Docker Desktop
3. **Logs não aparecem**: Verificar Filebeat e Logstash
4. **Serviços não iniciam**: Verificar health checks e logs

### Comandos Úteis
```bash
# Ver uso de recursos
docker stats

# Limpar tudo
docker system prune -a

# Entrar em container
docker exec -it condominio-servico-usuario bash

# Ver logs específicos
docker logs condominio-elasticsearch
```

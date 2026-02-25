#!/bin/bash

set -e

echo "🚀 Iniciando build dos serviços..."

# Build das imagens Docker
echo "📦 Build servico-identidade..."
docker build -t condominio/servico-identidade:latest ./servico-identidade

echo "📦 Build servico-usuario..."
docker build -t condominio/servico-usuario:latest ./servico-usuario

echo "📦 Build servico-encomenda..."
docker build -t condominio/servico-encomenda:latest ./servico-encomenda

echo "📦 Build servico-notificacao..."
docker build -t condominio/servico-notificacao:latest ./servico-notificacao

echo "📦 Build frontend..."
docker build -t condominio/frontend:latest ./frontend

echo "✅ Build concluído!"

# Subindo a infraestrutura
echo "🏗️ Subindo infraestrutura..."
docker-compose up -d postgres-identidade postgres-usuario postgres-encomenda postgres-notificacao
docker-compose up -d zookeeper kafka kafka-connect

echo "⏱️ Aguardando infraestrutura..."
sleep 30

# Subindo stack ELK
echo "📊 Subindo stack ELK..."
docker-compose up -d elasticsearch logstash kibana filebeat

echo "⏱️ Aguardando ELK..."
sleep 60

# Subindo serviços
echo "🏢 Subindo serviços da aplicação..."
docker-compose up -d servico-identidade
sleep 30
docker-compose up -d servico-usuario servico-encomenda servico-notificacao
sleep 30
docker-compose up -d frontend

# Subindo interfaces
echo "🖥️ Subindo interfaces de visualização..."
docker-compose up -d kafka-ui adminer

echo ""
echo "🎉 Sistema iniciado com sucesso!"
echo ""
echo "🌐 Aplicação:"
echo "   • Frontend:           http://localhost:3000"
echo ""
echo "📊 Interfaces disponíveis:"
echo "   • Kibana (Logs):      http://localhost:5601"
echo "   • Kafka UI:           http://localhost:8086"
echo "   • Adminer (DB):       http://localhost:8087"
echo ""
echo "🏢 Serviços (APIs):"
echo "   • Identidade:         http://localhost:8081"
echo "   • Usuário:            http://localhost:8082"
echo "   • Encomenda:          http://localhost:8083"
echo "   • Notificação:        http://localhost:8084"
echo ""
echo "💡 Para verificar logs: docker-compose logs -f [serviço]"
echo "💡 Para parar tudo:     docker-compose down"

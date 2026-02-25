#!/bin/bash

set -e

echo "Iniciando build dos servicos..."

# Build das imagens Docker
echo "Build servico-identidade..."
docker compose build servico-identidade

echo "Build servico-usuario..."
docker compose build servico-usuario

echo "Build servico-encomenda..."
docker compose build servico-encomenda

echo "Build servico-notificacao..."
docker compose build servico-notificacao

echo "Build frontend..."
docker compose build frontend

echo "Build docs-site..."
docker compose build docs-site

echo "Build concluido"

# Subindo a infraestrutura
echo "Subindo infraestrutura..."
docker compose up -d postgres-identidade postgres-usuario postgres-encomenda postgres-notificacao
docker compose up -d zookeeper kafka kafka-connect

echo "Aguardando infraestrutura..."
sleep 30

# Subindo stack ELK
echo "Subindo stack ELK..."
docker compose up -d elasticsearch logstash kibana filebeat

echo "Aguardando ELK..."
sleep 60

# Subindo servicos
echo "Subindo servicos da aplicacao..."
docker compose up -d servico-identidade
sleep 30
docker compose up -d servico-usuario servico-encomenda servico-notificacao
sleep 30
docker compose up -d frontend

# Subindo interfaces
echo "Subindo interfaces de visualizacao..."
docker compose up -d kafka-ui adminer docs-site

echo ""
echo "Sistema iniciado com sucesso"
echo ""
echo "Aplicacao:"
echo "   - Frontend:            http://localhost:3000"
echo ""
echo "Interfaces disponiveis:"
echo "   - Kibana (Logs):       http://localhost:5601"
echo "   - Kafka UI:            http://localhost:8086"
echo "   - Adminer (DB):        http://localhost:8087"
echo "   - Docs Tecnicos:       http://localhost:8090"
echo ""
echo "Servicos (APIs):"
echo "   - Identidade:          http://localhost:8081"
echo "   - Usuario:             http://localhost:8082"
echo "   - Encomenda:           http://localhost:8083"
echo "   - Notificacao:         http://localhost:8084"
echo ""
echo "Para verificar logs: docker compose logs -f [servico]"
echo "Para parar tudo:     docker compose down"

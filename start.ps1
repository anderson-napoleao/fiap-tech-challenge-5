param(
  [switch]$Build,
  [switch]$Logs,
  [string]$Service,
  [switch]$Down,
  [switch]$Status
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Yellow
}

if ($Build) {
    Write-Step "Build das imagens Docker"

    Write-Host "Build servico-identidade..." -ForegroundColor Blue
    docker compose build servico-identidade

    Write-Host "Build servico-usuario..." -ForegroundColor Blue
    docker compose build servico-usuario

    Write-Host "Build servico-encomenda..." -ForegroundColor Blue
    docker compose build servico-encomenda

    Write-Host "Build servico-notificacao..." -ForegroundColor Blue
    docker compose build servico-notificacao

    Write-Host "Build frontend..." -ForegroundColor Blue
    docker compose build frontend

    Write-Host "Build docs-site..." -ForegroundColor Blue
    docker compose build docs-site

    Write-Success "Build concluido"
}

if ($Down) {
    Write-Step "Parando e removendo todos os containers"
    docker compose down --volumes --remove-orphans
    Write-Success "Sistema parado"
    exit
}

if ($Status) {
    Write-Step "Status dos containers"
    docker compose ps
    exit
}

if ($Logs) {
    if ([string]::IsNullOrEmpty($Service)) {
        Write-Step "Logs de todos os servicos"
        docker compose logs -f
    } else {
        Write-Step "Logs do servico: $Service"
        docker compose logs -f $Service
    }
    exit
}

Write-Step "Iniciando sistema completo"

Write-Step "Subindo infraestrutura de dados"
docker compose up -d postgres-identidade postgres-usuario postgres-encomenda postgres-notificacao

Write-Step "Subindo infraestrutura Kafka"
docker compose up -d zookeeper kafka kafka-connect

Write-Info "Aguardando infraestrutura inicializar..."
Start-Sleep 30

Write-Step "Subindo stack ELK (Logs)"
docker compose up -d elasticsearch logstash kibana filebeat

Write-Info "Aguardando ELK inicializar..."
Start-Sleep 60

Write-Step "Subindo servicos da aplicacao"
docker compose up -d servico-identidade
Start-Sleep 30
docker compose up -d servico-usuario servico-encomenda servico-notificacao
Start-Sleep 30
docker compose up -d frontend

Write-Step "Subindo interfaces de visualizacao"
docker compose up -d kafka-ui adminer docs-site

Write-Success "Sistema iniciado com sucesso"

Write-Host ""
Write-Host "Aplicacao:" -ForegroundColor Magenta
Write-Host "   - Frontend:            http://localhost:3000"
Write-Host ""
Write-Host "Interfaces disponiveis:" -ForegroundColor Magenta
Write-Host "   - Kibana (Logs):       http://localhost:5601"
Write-Host "   - Kafka UI:            http://localhost:8086"
Write-Host "   - Adminer (DB):        http://localhost:8087"
Write-Host "   - Docs Tecnicos:       http://localhost:8090"
Write-Host ""
Write-Host "Servicos (APIs):" -ForegroundColor Magenta
Write-Host "   - Identidade:          http://localhost:8081"
Write-Host "   - Usuario:             http://localhost:8082"
Write-Host "   - Encomenda:           http://localhost:8083"
Write-Host "   - Notificacao:         http://localhost:8084"
Write-Host ""
Write-Host "Comandos uteis:" -ForegroundColor Cyan
Write-Host "   - Ver logs:            ./start.ps1 -Logs [-Service nome]"
Write-Host "   - Ver status:          ./start.ps1 -Status"
Write-Host "   - Parar sistema:       ./start.ps1 -Down"
Write-Host "   - Rebuild:             ./start.ps1 -Build"

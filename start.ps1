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
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Yellow
}

if ($Build) {
    Write-Step "Build das imagens Docker"
    
    Write-Host "📦 Build servico-identidade..." -ForegroundColor Blue
    docker build -t condominio/servico-identidade:latest ./servico-identidade
    
    Write-Host "📦 Build servico-usuario..." -ForegroundColor Blue
    docker build -t condominio/servico-usuario:latest ./servico-usuario
    
    Write-Host "📦 Build servico-encomenda..." -ForegroundColor Blue
    docker build -t condominio/servico-encomenda:latest ./servico-encomenda
    
    Write-Host "📦 Build servico-notificacao..." -ForegroundColor Blue
    docker build -t condominio/servico-notificacao:latest ./servico-notificacao
    
    Write-Host "📦 Build frontend..." -ForegroundColor Blue
    docker build -t condominio/frontend:latest ./frontend
    
    Write-Success "Build concluído!"
}

if ($Down) {
    Write-Step "Parando e removendo todos os containers"
    docker-compose down --volumes --remove-orphans
    Write-Success "Sistema parado!"
    exit
}

if ($Status) {
    Write-Step "Status dos containers"
    docker-compose ps
    exit
}

if ($Logs) {
    if ([string]::IsNullOrEmpty($Service)) {
        Write-Step "Logs de todos os serviços"
        docker-compose logs -f
    } else {
        Write-Step "Logs do serviço: $Service"
        docker-compose logs -f $Service
    }
    exit
}

# Start default flow
Write-Step "Iniciando sistema completo"

Write-Step "Subindo infraestrutura de dados"
docker-compose up -d postgres-identidade postgres-usuario postgres-encomenda postgres-notificacao

Write-Step "Subindo infraestrutura Kafka"
docker-compose up -d zookeeper kafka kafka-connect

Write-Info "Aguardando infraestrutura inicializar..."
Start-Sleep 30

Write-Step "Subindo stack ELK (Logs)"
docker-compose up -d elasticsearch logstash kibana filebeat

Write-Info "Aguardando ELK inicializar..."
Start-Sleep 60

Write-Step "Subindo serviços da aplicação"
docker-compose up -d servico-identidade
Start-Sleep 30
docker-compose up -d servico-usuario servico-encomenda servico-notificacao
Start-Sleep 30
docker-compose up -d frontend

Write-Step "Subindo interfaces de visualização"
docker-compose up -d kafka-ui adminer

Write-Success "Sistema iniciado com sucesso!"

Write-Host ""
Write-Host "🌐 Aplicação:" -ForegroundColor Magenta
Write-Host "   • Frontend:           http://localhost:3000"
Write-Host ""
Write-Host "📊 Interfaces disponíveis:" -ForegroundColor Magenta
Write-Host "   • Kibana (Logs):      http://localhost:5601"
Write-Host "   • Kafka UI:           http://localhost:8086"
Write-Host "   • Adminer (DB):       http://localhost:8087"
Write-Host ""
Write-Host "🏢 Serviços (APIs):" -ForegroundColor Magenta
Write-Host "   • Identidade:         http://localhost:8081"
Write-Host "   • Usuário:            http://localhost:8082"
Write-Host "   • Encomenda:          http://localhost:8083"
Write-Host "   • Notificação:        http://localhost:8084"
Write-Host ""
Write-Host "💡 Comandos úteis:" -ForegroundColor Cyan
Write-Host "   • Ver logs:          ./start.ps1 -Logs [-Service nome]"
Write-Host "   • Ver status:        ./start.ps1 -Status"
Write-Host "   • Parar sistema:     ./start.ps1 -Down"
Write-Host "   • Rebuild:           ./start.ps1 -Build"

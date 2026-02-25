param(
  [switch]$StartInfra
)

$ErrorActionPreference = "Stop"

$identityBase = "http://localhost:8081"
$encomendaBase = "http://localhost:8083"
$connectBase = "http://localhost:8084"
$connectorFile = "infra/debezium/encomenda-outbox-connector.json"
$connectorName = "encomenda-outbox-connector"

function Write-Step {
  param([string]$Message)
  Write-Host ""
  Write-Host "==> $Message" -ForegroundColor Cyan
}

function Wait-Healthy {
  param(
    [string]$Name,
    [string]$Url,
    [int]$TimeoutSeconds = 90
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    try {
      $res = Invoke-RestMethod -Uri $Url -Method Get
      if ($null -ne $res -and $res.status -eq "UP") {
        Write-Host "$Name OK ($Url)" -ForegroundColor Green
        return
      }
    } catch {
      Start-Sleep -Seconds 2
    }
  }

  throw "Timeout aguardando $Name em $Url"
}

function Ensure-OutboxConnector {
  param(
    [string]$ConnectBaseUrl,
    [string]$ConnectorJsonPath,
    [string]$Name
  )

  if (!(Test-Path $ConnectorJsonPath)) {
    throw "Arquivo do connector nao encontrado: $ConnectorJsonPath"
  }

  $doc = Get-Content $ConnectorJsonPath -Raw | ConvertFrom-Json
  $config = $doc.config

  # Evita falha conhecida no EventRouter com INT64 sem logical type.
  $config.PSObject.Properties.Remove("transforms.outbox.table.field.event.timestamp") | Out-Null
  $bodyCreate = @{
    name = $Name
    config = $config
  } | ConvertTo-Json -Depth 20
  $bodyConfig = $config | ConvertTo-Json -Depth 20

  $names = Invoke-RestMethod -Uri "$ConnectBaseUrl/connectors" -Method Get
  if ($names -contains $Name) {
    Invoke-RestMethod `
      -Uri "$ConnectBaseUrl/connectors/$Name/config" `
      -Method Put `
      -ContentType "application/json" `
      -Body $bodyConfig | Out-Null
    Write-Host "Connector atualizado: $Name" -ForegroundColor Green
  } else {
    Invoke-RestMethod `
      -Uri "$ConnectBaseUrl/connectors" `
      -Method Post `
      -ContentType "application/json" `
      -Body $bodyCreate | Out-Null
    Write-Host "Connector criado: $Name" -ForegroundColor Green
  }

  $deadline = (Get-Date).AddSeconds(45)
  while ((Get-Date) -lt $deadline) {
    try {
      $status = Invoke-RestMethod -Uri "$ConnectBaseUrl/connectors/$Name/status" -Method Get
      if ($status.tasks.Count -gt 0 -and $status.tasks[0].state -eq "RUNNING") {
        Write-Host "Connector RUNNING: $Name" -ForegroundColor Green
        return
      }
    } catch {
      $statusCode = $null
      if ($_.Exception -and $_.Exception.Response -and $_.Exception.Response.StatusCode) {
        $statusCode = [int]$_.Exception.Response.StatusCode
      }

      # Kafka Connect pode responder 404 por alguns segundos logo apos criar o connector.
      if ($statusCode -ne 404) {
        throw
      }
    }

    Start-Sleep -Seconds 2
  }

  try {
    $lastStatus = Invoke-RestMethod -Uri "$ConnectBaseUrl/connectors/$Name/status" -Method Get
    throw "Connector nao ficou RUNNING. Estado atual: $($lastStatus | ConvertTo-Json -Depth 20)"
  } catch {
    $statusCode = $null
    if ($_.Exception -and $_.Exception.Response -and $_.Exception.Response.StatusCode) {
      $statusCode = [int]$_.Exception.Response.StatusCode
    }

    if ($statusCode -eq 404) {
      throw "Connector $Name ainda sem status apos 45s. Kafka Connect pode nao ter inicializado totalmente."
    }

    throw
  }
}

function New-BasicAuthHeader {
  param(
    [string]$Username,
    [string]$Password
  )

  $raw = "$Username`:$Password"
  $encoded = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($raw))
  return @{
    Authorization = "Basic $encoded"
  }
}

if ($StartInfra) {
  Write-Step "Subindo stack CDC"
  docker compose -f infra/docker/docker-compose.cdc.yml up -d | Out-Null
}

Write-Step "Validando saude das APIs"
Wait-Healthy -Name "servico-identidade" -Url "$identityBase/actuator/health"
Wait-Healthy -Name "servico-encomenda" -Url "$encomendaBase/actuator/health"

Write-Step "Garantindo connector outbox"
Ensure-OutboxConnector -ConnectBaseUrl $connectBase -ConnectorJsonPath $connectorFile -Name $connectorName

$suffix = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$email = "lucas.amaral+$suffix@condominio.local"
$password = "Senha@123"

Write-Step "Criando usuario FUNCIONARIO (Lucas Amaral)"
$newUser = @{
  email = $email
  password = $password
  role = "FUNCIONARIO"
} | ConvertTo-Json
$adminHeaders = New-BasicAuthHeader -Username "admin" -Password "admin"
$createdUser = Invoke-RestMethod `
  -Uri "$identityBase/admin/users" `
  -Method Post `
  -Headers $adminHeaders `
  -ContentType "application/json" `
  -Body $newUser
Write-Host ("Usuario criado: id={0} email={1}" -f $createdUser.id, $createdUser.email) -ForegroundColor Green

Write-Step "Gerando token JWT"
$authReq = @{
  username = $email
  password = $password
} | ConvertTo-Json
$authRes = Invoke-RestMethod `
  -Uri "$identityBase/auth/token" `
  -Method Post `
  -ContentType "application/json" `
  -Body $authReq
$token = $authRes.access_token
if ([string]::IsNullOrWhiteSpace($token)) {
  throw "Falha ao gerar token JWT."
}
Write-Host "Token gerado com sucesso." -ForegroundColor Green

Write-Step "Registrando recebimento de encomenda"
$receberReq = @{
  nomeDestinatario = "Morador Lucas Amaral"
  apartamento = "101"
  bloco = "B"
  descricao = "Caixa pequena - teste CDC"
} | ConvertTo-Json
$headers = @{
  Authorization = "Bearer $token"
}
$encomenda = Invoke-RestMethod `
  -Uri "$encomendaBase/portaria/encomendas" `
  -Method Post `
  -Headers $headers `
  -ContentType "application/json" `
  -Body $receberReq
Write-Host ("Encomenda criada: id={0} status={1} recebidoPor={2}" -f $encomenda.id, $encomenda.status, $encomenda.recebidoPor) -ForegroundColor Green

$encomendaId = "$($encomenda.id)"

Write-Step "Validando persistencia no PostgreSQL"
docker exec condominio-postgres psql -U postgres -d condominio -c "SELECT id, nome_destinatario, apartamento, bloco, recebido_por, status, data_recebimento FROM encomendas WHERE id = $encomendaId;" 
docker exec condominio-postgres psql -U postgres -d condominio -c "SELECT id, aggregateid, type, event_version, event_timestamp, event_timestamp_ms FROM outbox_event WHERE aggregateid = '$encomendaId' ORDER BY event_timestamp DESC LIMIT 1;"

Write-Step "Validando evento no topico Kafka encomenda.recebida"
$kafkaOutput = docker exec condominio-kafka /kafka/bin/kafka-console-consumer.sh `
  --bootstrap-server kafka:9092 `
  --topic encomenda.recebida `
  --from-beginning `
  --timeout-ms 20000 `
  --max-messages 500 `
  --property print.key=true `
  --property print.headers=true

if ($kafkaOutput -match $encomendaId) {
  Write-Host "Evento encontrado no Kafka para a encomenda $encomendaId." -ForegroundColor Green
} else {
  Write-Host "Nao encontrei o id $encomendaId no output do consumidor. Verifique Kafka UI (http://localhost:8086)." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Smoke test concluido." -ForegroundColor Green
Write-Host "Visual: Adminer=http://localhost:8085 | Kafka UI=http://localhost:8086"

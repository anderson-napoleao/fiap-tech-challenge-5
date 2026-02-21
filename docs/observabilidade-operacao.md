# Operacao de Observabilidade

## Escopo

Guia operacional para:
- validar correlacao de trace entre `servico-usuario` e `servico-identidade`;
- validar contrato de logs JSON;
- executar rollout seguro;
- executar fallback rapido em caso de problema com JSON logging.

## Pre-requisitos

1. Java 21.
2. Maven 3.9+.
3. Portas livres:
   - `8081` (`servico-identidade`)
   - `8082` (`servico-usuario`)

## Cenario de teste inter-servicos (OBS-402)

### Passo 1 - Subir `servico-identidade` com log em arquivo

```powershell
mvn -pl servico-identidade spring-boot:run *> identidade.log
```

### Passo 2 - Subir `servico-usuario` com log em arquivo

```powershell
mvn -pl servico-usuario spring-boot:run *> usuario.log
```

### Passo 3 - Executar fluxo de cadastro no `servico-usuario`

```powershell
curl -i -X POST "http://localhost:8082/users" `
  -H "Content-Type: application/json" `
  -d '{
    "nomeCompleto":"Maria Observabilidade",
    "email":"maria.observabilidade@teste.com",
    "senha":"123456",
    "tipo":"MORADOR",
    "apartamento":"101",
    "bloco":"A"
  }'
```

### Passo 4 - Extrair `trace_id` do lado do `servico-usuario`

```powershell
$line = Select-String -Path usuario.log -Pattern '"event_type":"request_in".*"http.path":"/users".*"http.status_code":"201"' | Select-Object -Last 1
$trace = ($line.Line | ConvertFrom-Json).trace_id
$trace
```

### Passo 5 - Validar mesmo `trace_id` no `servico-identidade`

```powershell
Select-String -Path identidade.log -Pattern $trace
```

Resultado esperado:
- o mesmo `trace_id` aparece nos dois arquivos;
- em `usuario.log` no endpoint `/users`;
- em `identidade.log` no endpoint `/admin/users`.

## Evidencia esperada do cenario

- `trace_id` unico por fluxo.
- latencia e status registrados em ambos os lados.
- logs JSON parseaveis em ambos os servicos.

## Checklist de rollout seguro (OBS-403)

1. Executar testes antes de subir:
   - `mvn -pl servico-identidade,servico-usuario test`
2. Subir ambos os servicos localmente.
3. Verificar healthchecks:
   - `GET http://localhost:8081/actuator/health`
   - `GET http://localhost:8082/actuator/health`
4. Executar fluxo funcional basico:
   - criar usuario via `POST /users`.
5. Confirmar contrato minimo de log:
   - campos `@timestamp`, `level`, `service`, `env`, `trace_id`, `span_id`, `logger`, `message`.
6. Confirmar ausencia de dados sensiveis:
   - sem senha em logs;
   - sem JWT completo em logs.

## Fallback rapido (desabilitar JSON logging)

Opcao A (temporaria por startup):
1. criar um arquivo local de logback texto simples (exemplo `logback-plain.xml`);
2. iniciar com `logging.config` apontando para esse arquivo:

```powershell
mvn -pl servico-identidade spring-boot:run -Dspring-boot.run.arguments="--logging.config=file:./logback-plain.xml"
mvn -pl servico-usuario spring-boot:run -Dspring-boot.run.arguments="--logging.config=file:./logback-plain.xml"
```

Opcao B (rollback):
1. voltar para o ultimo commit estavel;
2. reexecutar `mvn test` dos modulos afetados;
3. subir novamente os servicos.

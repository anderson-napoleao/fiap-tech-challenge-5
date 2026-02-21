# Padrao de Observabilidade do Projeto

## Escopo

Este documento define o padrao unico de observabilidade para o monorepo `sistema-condominio`.

Objetivos:
- padronizar logs JSON;
- garantir correlacao por trace entre servicos;
- evitar vazamento de dados sensiveis;
- manter compatibilidade com Clean Architecture.

## Servicos e Nome Canonico

Cada modulo deve publicar logs com `service` no valor abaixo:

- `servico-identidade`
- `servico-usuario`
- `servico-encomenda`
- `servico-notificacao`

## Formato de Log

- Formato: JSON por linha (um evento por linha).
- Charset: UTF-8.
- Timestamp: ISO-8601 em UTC.
- Campos em `snake_case` para consistencia.

## Campos Obrigatorios

Todo evento de log deve conter:

- `@timestamp`
- `level`
- `service`
- `env`
- `trace_id`
- `span_id`
- `logger`
- `message`

## Campos Recomendados por Contexto

Eventos HTTP de entrada:

- `event_type=request_in`
- `http.method`
- `http.path`
- `http.status_code`
- `event.duration_ms`

Eventos HTTP de saida (chamadas externas):

- `event_type=request_out`
- `peer.service`
- `http.method`
- `http.url`
- `http.status_code`
- `event.duration_ms`

Eventos de erro:

- `event_type=error`
- `error.type`
- `error.message`
- `error.stack_trace` (somente quando necessario para diagnostico)

## Padrao de Tracing

- Padrao principal de propagacao: W3C Trace Context (`traceparent`, `tracestate`).
- Em requisicao recebida:
  - se ja houver cabecalho de trace, reutilizar;
  - se nao houver, iniciar novo trace.
- Em chamada HTTP entre servicos:
  - sempre propagar contexto de trace;
  - manter o mesmo `trace_id` no fluxo encadeado.

## Niveis de Log

- `DEBUG`: diagnostico detalhado local, desligado por padrao em producao.
- `INFO`: eventos normais de ciclo de requisicao e operacoes de negocio.
- `WARN`: falhas esperadas de regra/validacao/conflito sem queda do servico.
- `ERROR`: falhas nao esperadas, excecoes e indisponibilidade de dependencia.

## Politica de Dados Sensiveis

Nunca logar:
- senha em qualquer formato;
- token JWT completo;
- segredos/chaves (`secret`, `private_key`, credenciais).

Mascaramento recomendado:
- email: manter 3 primeiros caracteres e dominio, mascarar restante local.
- cpf: manter apenas 3 ultimos digitos.
- authorization header: logar apenas tipo (`Bearer`), sem token.

## Eventos Minimos por Endpoint HTTP

Para cada endpoint:
- 1 log no fim da requisicao com status e duracao;
- 1 log adicional apenas em caso de erro;
- sem logging de payload completo por padrao.

## Conformidade com Arquitetura

- Implementar observabilidade apenas em `config`, `adapter`, `infrastructure`.
- `domain` e `application` nao devem depender de Spring, logback ou framework de observabilidade.
- Regras de ArchUnit existentes devem continuar passando sem relaxamento.

## Exemplo de Log HTTP de Entrada

```json
{
  "@timestamp": "2026-02-21T15:02:11.532Z",
  "level": "INFO",
  "service": "servico-identidade",
  "env": "local",
  "trace_id": "4f2c7ec4a3a5f9d0f4cc1181df778111",
  "span_id": "65bd0f2f3f832ac1",
  "logger": "br.com.condominio.identidade.infrastructure.observability.HttpLoggingFilter",
  "event_type": "request_in",
  "http.method": "POST",
  "http.path": "/auth/token",
  "http.status_code": 200,
  "event.duration_ms": 18,
  "message": "request completed"
}
```

## Exemplo de Log de Erro

```json
{
  "@timestamp": "2026-02-21T15:03:00.122Z",
  "level": "WARN",
  "service": "servico-usuario",
  "env": "local",
  "trace_id": "4f2c7ec4a3a5f9d0f4cc1181df778111",
  "span_id": "abcb0f2f3f832fff",
  "logger": "br.com.condominio.servico.usuario.infrastructure.web.error.GlobalExceptionHandler",
  "event_type": "error",
  "error.type": "ConflictException",
  "error.message": "email ja cadastrado",
  "message": "request failed with business conflict"
}
```

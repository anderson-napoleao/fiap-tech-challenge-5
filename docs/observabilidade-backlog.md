# Backlog Tecnico de Observabilidade

## Objetivo

Adicionar observabilidade padronizada no projeto com:
- logs estruturados em JSON;
- correlacao por trace/span entre requisicoes;
- padrao unico de campos de log entre modulos;
- implementacao sem violar Clean Architecture.

## Convencoes Globais

- Campos minimos do log JSON: `@timestamp`, `level`, `service`, `env`, `traceId`, `spanId`, `logger`, `message`.
- Campos HTTP quando aplicavel: `http.method`, `http.path`, `http.status_code`, `event.duration_ms`.
- Nao logar dados sensiveis: senha, token JWT completo, secret.
- Observabilidade apenas em `config`, `adapter` e `infrastructure`.
- `domain` e `application` sem dependencia de Spring/logback/tracing.

## Fase 1 - Fundacao do Monorepo

### OBS-001 - Definir padrao de observabilidade

- Modulo: raiz
- Entrega: documento de padrao unico de logs e tracing para todos os modulos.
- Status: concluido em 2026-02-21
- Evidencia: `docs/observabilidade-padrao.md`
- Criterios de aceite:
  - padrao de campos JSON definido;
  - politica de mascaramento de dados sensiveis definida;
  - convencao de nome de servico definida (`servico-identidade`, `servico-usuario`, `servico-encomenda`, `servico-notificacao`).

### OBS-002 - Padronizar propriedades baseline nos modulos Spring Boot

- Modulo: `servico-identidade`, `servico-usuario`
- Entrega: propriedades iniciais de observabilidade em `application.yml`.
- Status: concluido em 2026-02-21
- Evidencia: `servico-identidade/src/main/resources/application.yml`, `servico-usuario/src/main/resources/application.yml`
- Criterios de aceite:
  - `spring.application.name` configurado em ambos os modulos;
  - configuracoes de tracing e exposicao do actuator aplicadas sem quebrar testes;
  - build de cada modulo Spring Boot concluindo com sucesso.

## Fase 2 - Servico Identidade

### OBS-101 - Adicionar encoder JSON no logback

- Modulo: `servico-identidade`
- Entrega: `logback-spring.xml` com appender JSON.
- Status: concluido em 2026-02-21
- Evidencia: `servico-identidade/pom.xml`, `servico-identidade/src/main/resources/logback-spring.xml`
- Criterios de aceite:
  - dependencia `logstash-logback-encoder` adicionada no `pom.xml`;
  - logs de inicializacao e requisicao saem em JSON valido;
  - campos minimos globais presentes.

### OBS-102 - Ativar tracing e correlacao de logs

- Modulo: `servico-identidade`
- Entrega: tracing ativo com `traceId`/`spanId` no MDC.
- Status: concluido em 2026-02-21
- Evidencia: `servico-identidade/pom.xml`, `servico-identidade/src/main/resources/logback-spring.xml`
- Criterios de aceite:
  - toda requisicao HTTP recebida gera `traceId` e `spanId`;
  - logs da mesma requisicao compartilham o mesmo `traceId`;
  - endpoint de health continua funcionando em `GET /actuator/health`.

### OBS-103 - Log estruturado de entrada HTTP

- Modulo: `servico-identidade`
- Entrega: filtro/interceptor para logs de request/response.
- Status: concluido em 2026-02-21
- Evidencia: `servico-identidade/src/main/java/br/com/condominio/identidade/infrastructure/observability/HttpLoggingInterceptor.java`, `servico-identidade/src/main/java/br/com/condominio/identidade/infrastructure/observability/ObservabilityWebMvcConfig.java`
- Criterios de aceite:
  - log por requisicao contendo metodo, path, status e duracao;
  - excecoes sao logadas com stacktrace e correlacao por trace;
  - nao ha log de corpo de request com senha em `/auth/token`.

### OBS-104 - Instrumentacao em bordas de entrada

- Modulo: `servico-identidade`
- Entrega: logs de negocio em controladores.
- Status: concluido em 2026-02-21
- Evidencia: `servico-identidade/src/main/java/br/com/condominio/identidade/adapter/in/web/AuthController.java`, `servico-identidade/src/main/java/br/com/condominio/identidade/adapter/in/web/AdminUserController.java`
- Criterios de aceite:
  - `AuthController` e `AdminUserController` com logs de inicio/fim de operacao;
  - falhas de autenticacao geram evento de warning sem expor credenciais;
  - logs mantem correlacao de trace.

## Fase 3 - Servico Usuario

### OBS-201 - Habilitar base de observabilidade no modulo

- Modulo: `servico-usuario`
- Entrega: dependencias e configuracoes para logs/tracing.
- Status: concluido em 2026-02-21
- Evidencia: `servico-usuario/pom.xml`, `servico-usuario/src/main/resources/logback-spring.xml`
- Criterios de aceite:
  - `spring-boot-starter-actuator` adicionado no `pom.xml`;
  - `logstash-logback-encoder` adicionado no `pom.xml`;
  - `logback-spring.xml` criado com padrao JSON comum.

### OBS-202 - Tracing de entrada HTTP

- Modulo: `servico-usuario`
- Entrega: correlacao de logs por trace em todas as rotas.
- Status: concluido em 2026-02-21
- Evidencia: `servico-usuario/src/main/java/br/com/condominio/servico/usuario/infrastructure/observability/HttpLoggingInterceptor.java`, `servico-usuario/src/main/java/br/com/condominio/servico/usuario/infrastructure/observability/ObservabilityWebMvcConfig.java`, `servico-usuario/src/main/java/br/com/condominio/servico/usuario/infrastructure/web/error/GlobalExceptionHandler.java`
- Criterios de aceite:
  - requests em `/users` e `/users/me` geram `traceId`/`spanId`;
  - logs de erro em `GlobalExceptionHandler` preservam `traceId`;
  - parsing JSON dos logs passa sem erro.

### OBS-203 - Propagacao de trace para chamada externa

- Modulo: `servico-usuario`
- Entrega: propagacao de contexto de trace em chamadas para `servico-identidade`.
- Status: concluido em 2026-02-21
- Evidencia: `servico-usuario/src/main/java/br/com/condominio/servico/usuario/adapter/out/IdentityGatewayAdapter.java`
- Criterios de aceite:
  - chamada de `IdentityGatewayAdapter` envia cabecalhos de trace;
  - request encadeada `servico-usuario -> servico-identidade` pode ser correlacionada por `traceId`;
  - latencia e status da chamada externa sao logados em JSON.

### OBS-204 - Instrumentacao em pontos de borda

- Modulo: `servico-usuario`
- Entrega: logs estruturados de operacoes de API.
- Status: concluido em 2026-02-21
- Evidencia: `servico-usuario/src/main/java/br/com/condominio/servico/usuario/adapter/in/web/UserController.java`, `servico-usuario/src/main/java/br/com/condominio/servico/usuario/infrastructure/web/error/GlobalExceptionHandler.java`, `servico-usuario/src/main/java/br/com/condominio/servico/usuario/adapter/out/IdentityGatewayAdapter.java`
- Criterios de aceite:
  - `UserController` registra eventos principais por operacao;
  - conflito e not found geram logs adequados de nivel `WARN`;
  - sem log de senha e sem log de JWT completo.

## Fase 4 - Blueprint para Modulos Sem Runtime HTTP

### OBS-301 - Preparar blueprint de observabilidade para encomenda

- Modulo: `servico-encomenda`
- Entrega: checklist de ativacao para quando o modulo virar Spring Boot.
- Criterios de aceite:
  - blueprint descreve dependencias e arquivos a criar;
  - regras de arquitetura permanecem inalteradas;
  - nenhum framework adicionado no estado atual do modulo.

### OBS-302 - Preparar blueprint de observabilidade para notificacao

- Modulo: `servico-notificacao`
- Entrega: checklist de ativacao para quando o modulo virar Spring Boot.
- Criterios de aceite:
  - blueprint descreve dependencias e arquivos a criar;
  - regras de arquitetura permanecem inalteradas;
  - nenhum framework adicionado no estado atual do modulo.

## Fase 5 - Qualidade, Testes e Operacao

### OBS-401 - Testes de contrato de logs JSON

- Modulo: `servico-identidade`, `servico-usuario`
- Entrega: testes que validam formato/campos obrigatorios de logs.
- Criterios de aceite:
  - teste falha quando campo obrigatorio de log nao existe;
  - teste valida que logs sao JSON parseavel;
  - teste cobre ao menos uma rota de sucesso e uma de erro por modulo.

### OBS-402 - Teste de correlacao inter-servicos

- Modulo: `servico-identidade`, `servico-usuario`
- Entrega: cenario de teste com correlacao ponta a ponta.
- Criterios de aceite:
  - fluxo de cadastro de usuario prova correlacao entre os dois servicos;
  - `traceId` aparece em ambos os lados da chamada;
  - evidencias de teste documentadas no README operacional.

### OBS-403 - Checklist de rollout seguro

- Modulo: raiz
- Entrega: passos para subir local e validar observabilidade.
- Criterios de aceite:
  - comandos de subida local documentados;
  - passos de verificacao de logs e trace documentados;
  - estrategia de fallback para desabilitar JSON logging documentada.

## Ordem Recomendada de Execucao

1. OBS-001
2. OBS-002
3. OBS-101
4. OBS-102
5. OBS-103
6. OBS-104
7. OBS-201
8. OBS-202
9. OBS-203
10. OBS-204
11. OBS-301
12. OBS-302
13. OBS-401
14. OBS-402
15. OBS-403

## Definicao de Pronto (DoD)

- build Maven verde no modulo alterado;
- testes existentes e novos passando;
- regras de ArchUnit passando;
- sem violacao de Clean Architecture;
- logs em JSON com `traceId`/`spanId` verificados em execucao local.

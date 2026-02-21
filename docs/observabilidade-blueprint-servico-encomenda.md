# Blueprint de Observabilidade - servico-encomenda

## Estado atual

- Modulo sem runtime Spring Boot.
- Modulo focado em regras e testes de arquitetura.
- Nenhuma dependencia de observabilidade deve ser adicionada neste momento.

## Objetivo do blueprint

Definir o caminho de ativacao de observabilidade para quando `servico-encomenda` virar um servico executavel (Spring Boot).

## Guardrails de arquitetura

- Nao adicionar dependencia de framework em `domain` e `application`.
- Observabilidade apenas em `config`, `adapter` e `infrastructure`.
- Regras de ArchUnit devem continuar verdes sem relaxamento.

## Checklist de ativacao (quando houver runtime)

1. Atualizar `servico-encomenda/pom.xml` com:
   - `spring-boot-starter-web` (se exposicao HTTP);
   - `spring-boot-starter-actuator`;
   - `micrometer-tracing-bridge-brave`;
   - `logstash-logback-encoder`.
2. Criar `servico-encomenda/src/main/resources/application.yml` com:
   - `spring.application.name: servico-encomenda`;
   - `management.endpoints.web.exposure.include: health,info`;
   - `management.tracing.sampling.probability: 1.0`.
3. Criar `servico-encomenda/src/main/resources/logback-spring.xml` no mesmo padrao JSON dos servicos existentes.
4. Criar interceptor de entrada HTTP em:
   - `servico-encomenda/src/main/java/.../infrastructure/observability/HttpLoggingInterceptor.java`;
   - `servico-encomenda/src/main/java/.../infrastructure/observability/ObservabilityWebMvcConfig.java`.
5. Garantir logs de borda:
   - controllers/rest endpoints em `adapter.in.web`;
   - gateways/adapters de saida em `adapter.out`.
6. Se houver chamadas externas HTTP:
   - propagar `traceparent`;
   - logar `event_type=request_out`, status e latencia.

## Checklist de validacao

1. `mvn -pl servico-encomenda test` com sucesso.
2. Regras ArchUnit de Clean Architecture passando.
3. Logs em JSON parseavel com `trace_id` e `span_id`.
4. Ao menos um fluxo de sucesso e um de erro com correlacao por trace.

## Nao fazer no estado atual

- Nao adicionar Spring Boot agora.
- Nao criar classes de observabilidade sem runtime.
- Nao introduzir logging em `domain` ou `application`.

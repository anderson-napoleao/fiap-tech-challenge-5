# Blueprint de Observabilidade - servico-notificacao

## Estado atual

- Modulo sem runtime Spring Boot.
- Modulo focado em regras e testes de arquitetura.
- Nenhuma dependencia de observabilidade deve ser adicionada neste momento.

## Objetivo do blueprint

Definir ativacao de observabilidade para quando `servico-notificacao` virar um servico executavel (Spring Boot), HTTP ou assincrono.

## Guardrails de arquitetura

- `domain` e `application` permanecem puros.
- Observabilidade apenas em `config`, `adapter` e `infrastructure`.
- Regras ArchUnit devem continuar passando.

## Checklist de ativacao (quando houver runtime)

1. Atualizar `servico-notificacao/pom.xml` com:
   - `spring-boot-starter-actuator`;
   - `micrometer-tracing-bridge-brave`;
   - `logstash-logback-encoder`;
   - `spring-boot-starter-web` (se houver API HTTP);
   - starter de mensageria do broker escolhido (se consumidor/produtor async).
2. Criar `servico-notificacao/src/main/resources/application.yml` com:
   - `spring.application.name: servico-notificacao`;
   - `management.endpoints.web.exposure.include: health,info` (quando HTTP);
   - `management.tracing.sampling.probability: 1.0`.
3. Criar `servico-notificacao/src/main/resources/logback-spring.xml` com padrao JSON comum do projeto.
4. Se houver API HTTP:
   - criar `HttpLoggingInterceptor` e `ObservabilityWebMvcConfig`.
5. Se houver processamento assincrono:
   - extrair `traceparent` do header da mensagem;
   - criar novo trace quando nao houver contexto;
   - logar `event_type=message_in` e `event_type=message_out` com destino e latencia.
6. Em adapters de saida (email/sms/push/http):
   - logar status da entrega, tentativas e tempo de execucao;
   - nunca logar payload sensivel de notificacao.

## Checklist de validacao

1. `mvn -pl servico-notificacao test` com sucesso.
2. ArchUnit verde.
3. Logs JSON parseaveis contendo `trace_id` e `span_id`.
4. Correlacao de trace validada em ao menos um fluxo fim-a-fim.

## Nao fazer no estado atual

- Nao adicionar framework agora.
- Nao criar interceptors/filters sem runtime.
- Nao inserir dependencias de observabilidade em `domain` ou `application`.

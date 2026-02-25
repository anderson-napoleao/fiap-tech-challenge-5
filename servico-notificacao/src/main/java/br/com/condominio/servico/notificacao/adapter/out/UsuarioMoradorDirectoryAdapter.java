package br.com.condominio.servico.notificacao.adapter.out;

import br.com.condominio.servico.notificacao.application.port.out.MoradorDirectoryPort;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adaptador de saida para persistencia ou integracao externa.
 */
@Component
public class UsuarioMoradorDirectoryAdapter implements MoradorDirectoryPort {

  private static final Logger log = LoggerFactory.getLogger(UsuarioMoradorDirectoryAdapter.class);
  private static final String USUARIO_SERVICE_NAME = "servico-usuario";
  private static final String IDENTITY_SERVICE_NAME = "servico-identidade";

  private final RestClient restClient;
  private final Tracer tracer;
  private final String usuarioBaseUrl;
  private final String identidadeBaseUrl;
  private final String serviceUsername;
  private final String servicePassword;

  public UsuarioMoradorDirectoryAdapter(
      RestClient.Builder restClientBuilder,
      Tracer tracer,
      @Value("${usuario.base-url:http://localhost:8082}") String usuarioBaseUrl,
      @Value("${identity.base-url:http://localhost:8081}") String identidadeBaseUrl,
      @Value("${identity.service-username:admin}") String serviceUsername,
      @Value("${identity.service-password:admin}") String servicePassword
  ) {
    this.restClient = restClientBuilder.build();
    this.tracer = tracer;
    this.usuarioBaseUrl = usuarioBaseUrl;
    this.identidadeBaseUrl = identidadeBaseUrl;
    this.serviceUsername = serviceUsername;
    this.servicePassword = servicePassword;
  }

  @Override
  public List<Morador> listarMoradoresPorUnidade(String bloco, String apartamento) {
    String token = gerarTokenServico();
    String url = usuarioBaseUrl + "/interno/usuarios/moradores?bloco={bloco}&apartamento={apartamento}";
    long startNanos = System.nanoTime();

    try {
      MoradorUnidadeResponse[] response = restClient.get()
          .uri(url, bloco, apartamento)
          .headers(headers -> {
            headers.setBearerAuth(token);
            applyTraceHeaders(headers);
          })
          .retrieve()
          .body(MoradorUnidadeResponse[].class);

      long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
      logOutboundSuccess(USUARIO_SERVICE_NAME, "GET", url, durationMillis);

      if (response == null || response.length == 0) {
        return List.of();
      }

      return Arrays.stream(response)
          .map(item -> new Morador(item.identityId(), item.nomeCompleto(), item.email()))
          .toList();
    } catch (RuntimeException exception) {
      long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
      logOutboundFailure(USUARIO_SERVICE_NAME, "GET", url, durationMillis, exception);
      throw exception;
    }
  }

  private String gerarTokenServico() {
    String url = identidadeBaseUrl + "/auth/token";
    long startNanos = System.nanoTime();

    try {
      TokenResponse response = restClient.post()
          .uri(url)
          .contentType(MediaType.APPLICATION_JSON)
          .headers(this::applyTraceHeaders)
          .body(new TokenRequest(serviceUsername, servicePassword))
          .retrieve()
          .body(TokenResponse.class);

      if (response == null || response.access_token() == null || response.access_token().isBlank()) {
        throw new IllegalStateException("Falha ao gerar token de servico para consulta de moradores");
      }

      long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
      logOutboundSuccess(IDENTITY_SERVICE_NAME, "POST", url, durationMillis);
      return response.access_token();
    } catch (RuntimeException exception) {
      long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
      logOutboundFailure(IDENTITY_SERVICE_NAME, "POST", url, durationMillis, exception);
      throw exception;
    }
  }

  private void applyTraceHeaders(HttpHeaders headers) {
    String traceId = null;
    String spanId = null;

    Span currentSpan = tracer.currentSpan();
    if (currentSpan != null) {
      TraceContext context = currentSpan.context();
      if (context != null) {
        traceId = context.traceId();
        spanId = context.spanId();
      }
    }

    if (isBlank(traceId) || isBlank(spanId)) {
      traceId = MDC.get("traceId");
      spanId = MDC.get("spanId");
    }

    if (isBlank(traceId) || isBlank(spanId)) {
      return;
    }

    if (!headers.containsKey("traceparent")) {
      headers.set("traceparent", "00-" + traceId + "-" + spanId + "-01");
    }
  }

  private void logOutboundSuccess(String peerService, String method, String url, long durationMillis) {
    try (MDC.MDCCloseable eventType = MDC.putCloseable("event_type", "request_out");
         MDC.MDCCloseable peer = MDC.putCloseable("peer.service", peerService);
         MDC.MDCCloseable httpMethod = MDC.putCloseable("http.method", method);
         MDC.MDCCloseable httpUrl = MDC.putCloseable("http.url", url);
         MDC.MDCCloseable duration = MDC.putCloseable("event.duration_ms", String.valueOf(durationMillis))) {
      log.info("outbound request completed");
    }
  }

  private void logOutboundFailure(
      String peerService,
      String method,
      String url,
      long durationMillis,
      RuntimeException exception
  ) {
    try (MDC.MDCCloseable eventType = MDC.putCloseable("event_type", "request_out");
         MDC.MDCCloseable peer = MDC.putCloseable("peer.service", peerService);
         MDC.MDCCloseable httpMethod = MDC.putCloseable("http.method", method);
         MDC.MDCCloseable httpUrl = MDC.putCloseable("http.url", url);
         MDC.MDCCloseable duration = MDC.putCloseable("event.duration_ms", String.valueOf(durationMillis));
         MDC.MDCCloseable errorType = MDC.putCloseable("error.type", exception.getClass().getSimpleName())) {
      log.error("outbound request failed", exception);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private record MoradorUnidadeResponse(
      String identityId,
      String nomeCompleto,
      String email
  ) {
  }

  private record TokenRequest(String username, String password) {
  }

  private record TokenResponse(String access_token, String token_type, long expires_in) {
  }
}

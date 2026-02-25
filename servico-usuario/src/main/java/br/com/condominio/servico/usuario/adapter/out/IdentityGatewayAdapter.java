package br.com.condominio.servico.usuario.adapter.out;

import br.com.condominio.servico.usuario.application.port.out.IdentityGatewayPort;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adaptador de saida para persistencia ou integracao externa.
 */
@Component
public class IdentityGatewayAdapter implements IdentityGatewayPort {

  private static final Logger log = LoggerFactory.getLogger(IdentityGatewayAdapter.class);
  private static final String IDENTITY_SERVICE_NAME = "servico-identidade";

  private final RestClient restClient;
  private final Tracer tracer;
  private final String identityBaseUrl;
  private final String identityAdminUsername;
  private final String identityAdminPassword;

  public IdentityGatewayAdapter(
      RestClient.Builder restClientBuilder,
      Tracer tracer,
      @Value("${identity.base-url:http://localhost:8081}") String identityBaseUrl,
      @Value("${identity.admin.username:admin}") String identityAdminUsername,
      @Value("${identity.admin.password:admin}") String identityAdminPassword
  ) {
    this.restClient = restClientBuilder.build();
    this.tracer = tracer;
    this.identityBaseUrl = identityBaseUrl;
    this.identityAdminUsername = identityAdminUsername;
    this.identityAdminPassword = identityAdminPassword;
  }

  @Override
  public CriarIdentidadeResult criarUsuario(CriarIdentidadeCommand command) {
    String url = identityBaseUrl + "/admin/users";
    long startNanos = System.nanoTime();

    try {
      ResponseEntity<CreateIdentityResponse> responseEntity = restClient
          .post()
          .uri(url)
          .headers(this::applyCommonHeaders)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new CreateIdentityRequest(command.email(), command.senha(), command.role()))
          .retrieve()
          .toEntity(CreateIdentityResponse.class);

      CreateIdentityResponse response = responseEntity.getBody();
      if (response == null || response.id() == null) {
        throw new IllegalStateException("Falha ao criar usuario no servico-identidade");
      }

      long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
      logOutboundSuccess("POST", url, responseEntity.getStatusCode().value(), durationMillis);
      return new CriarIdentidadeResult(response.id(), response.email());
    } catch (RuntimeException exception) {
      long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
      logOutboundFailure("POST", url, durationMillis, exception);
      throw exception;
    }
  }

  @Override
  public void removerUsuario(String identityId) {
    String url = identityBaseUrl + "/admin/users/" + identityId;
    long startNanos = System.nanoTime();

    try {
      ResponseEntity<Void> responseEntity = restClient
          .delete()
          .uri(identityBaseUrl + "/admin/users/{id}", identityId)
          .headers(this::applyCommonHeaders)
          .retrieve()
          .toBodilessEntity();

      long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
      logOutboundSuccess("DELETE", url, responseEntity.getStatusCode().value(), durationMillis);
    } catch (RuntimeException exception) {
      long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
      logOutboundFailure("DELETE", url, durationMillis, exception);
      throw exception;
    }
  }

  @Override
  public void desabilitarUsuario(String identityId) {
    String url = identityBaseUrl + "/admin/users/" + identityId + "/disable";
    long startNanos = System.nanoTime();

    try {
      ResponseEntity<Void> responseEntity = restClient
          .patch()
          .uri(identityBaseUrl + "/admin/users/{id}/disable", identityId)
          .headers(this::applyCommonHeaders)
          .retrieve()
          .toBodilessEntity();

      long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
      logOutboundSuccess("PATCH", url, responseEntity.getStatusCode().value(), durationMillis);
    } catch (RuntimeException exception) {
      long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
      logOutboundFailure("PATCH", url, durationMillis, exception);
      throw exception;
    }
  }

  private void applyCommonHeaders(HttpHeaders headers) {
    applyAdminBasicAuth(headers);
    applyTraceHeaders(headers);
  }

  private void applyAdminBasicAuth(HttpHeaders headers) {
    headers.setBasicAuth(identityAdminUsername, identityAdminPassword);
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

  private void logOutboundSuccess(String method, String url, int statusCode, long durationMillis) {
    try (MDC.MDCCloseable eventType = MDC.putCloseable("event_type", "request_out");
         MDC.MDCCloseable peerService = MDC.putCloseable("peer.service", IDENTITY_SERVICE_NAME);
         MDC.MDCCloseable httpMethod = MDC.putCloseable("http.method", method);
         MDC.MDCCloseable httpUrl = MDC.putCloseable("http.url", url);
         MDC.MDCCloseable httpStatus = MDC.putCloseable("http.status_code", String.valueOf(statusCode));
         MDC.MDCCloseable duration = MDC.putCloseable("event.duration_ms", String.valueOf(durationMillis))) {
      if (statusCode >= 500) {
        log.error("outbound request completed with server error");
      } else if (statusCode >= 400) {
        log.warn("outbound request completed with client error");
      } else {
        log.info("outbound request completed");
      }
    }
  }

  private void logOutboundFailure(String method, String url, long durationMillis, RuntimeException exception) {
    try (MDC.MDCCloseable eventType = MDC.putCloseable("event_type", "request_out");
         MDC.MDCCloseable peerService = MDC.putCloseable("peer.service", IDENTITY_SERVICE_NAME);
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

  private record CreateIdentityRequest(String email, String password, String role) {
  }

  private record CreateIdentityResponse(String id, String email) {
  }
}

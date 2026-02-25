package br.com.condominio.servico.encomenda.infrastructure.observability;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Implementa observabilidade para logs e correlacao de rastreamento.
 */
@Component
public class HttpLoggingInterceptor implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(HttpLoggingInterceptor.class);
  private static final String START_TIME_ATTRIBUTE = "http.logging.startNanos";
  private static final String GENERATED_TRACE_ATTRIBUTE = "http.logging.generatedTrace";
  private static final String TRACE_ID_KEY = "traceId";
  private static final String SPAN_ID_KEY = "spanId";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime());
    ensureTraceCorrelation(request);
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      @Nullable Exception ex
  ) {
    long startNanos = readStartTime(request);
    long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
    int statusCode = response.getStatus();
    Throwable throwable = extractThrowable(request, ex);

    try (MDC.MDCCloseable eventType = MDC.putCloseable("event_type", "request_in");
         MDC.MDCCloseable method = MDC.putCloseable("http.method", request.getMethod());
         MDC.MDCCloseable path = MDC.putCloseable("http.path", request.getRequestURI());
         MDC.MDCCloseable status = MDC.putCloseable("http.status_code", String.valueOf(statusCode));
         MDC.MDCCloseable duration = MDC.putCloseable("event.duration_ms", String.valueOf(durationMillis))) {
      if (throwable != null) {
        log.error("request failed with unhandled exception", throwable);
        return;
      }

      if (statusCode >= 500) {
        log.error("request completed with server error");
      } else if (statusCode >= 400) {
        log.warn("request completed with client error");
      } else {
        log.info("request completed");
      }
    } finally {
      clearGeneratedTrace(request);
    }
  }

  private long readStartTime(HttpServletRequest request) {
    Object value = request.getAttribute(START_TIME_ATTRIBUTE);
    if (value instanceof Long start) {
      return start;
    }
    return System.nanoTime();
  }

  private Throwable extractThrowable(HttpServletRequest request, @Nullable Exception ex) {
    if (ex != null) {
      return ex;
    }

    Object error = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    if (error instanceof Throwable throwable) {
      return throwable;
    }

    return null;
  }

  private void ensureTraceCorrelation(HttpServletRequest request) {
    if (!isBlank(MDC.get(TRACE_ID_KEY)) && !isBlank(MDC.get(SPAN_ID_KEY))) {
      return;
    }

    TraceValues values = extractTraceFromHeader(request.getHeader("traceparent"));
    if (values == null) {
      values = new TraceValues(generateTraceId(), generateSpanId());
    }

    MDC.put(TRACE_ID_KEY, values.traceId());
    MDC.put(SPAN_ID_KEY, values.spanId());
    request.setAttribute(GENERATED_TRACE_ATTRIBUTE, true);
  }

  private void clearGeneratedTrace(HttpServletRequest request) {
    Object generated = request.getAttribute(GENERATED_TRACE_ATTRIBUTE);
    if (generated instanceof Boolean generatedTrace && generatedTrace) {
      MDC.remove(TRACE_ID_KEY);
      MDC.remove(SPAN_ID_KEY);
    }
  }

  private TraceValues extractTraceFromHeader(@Nullable String traceparent) {
    if (isBlank(traceparent)) {
      return null;
    }

    String[] parts = traceparent.split("-");
    if (parts.length != 4) {
      return null;
    }

    String traceId = parts[1];
    String spanId = parts[2];
    if (traceId.length() != 32 || spanId.length() != 16) {
      return null;
    }
    return new TraceValues(traceId, spanId);
  }

  private String generateTraceId() {
    return hex16(ThreadLocalRandom.current().nextLong()) + hex16(ThreadLocalRandom.current().nextLong());
  }

  private String generateSpanId() {
    return hex16(ThreadLocalRandom.current().nextLong());
  }

  private String hex16(long value) {
    return String.format("%016x", value);
  }

  private boolean isBlank(@Nullable String value) {
    return value == null || value.isBlank();
  }

  private record TraceValues(String traceId, String spanId) {
  }
}

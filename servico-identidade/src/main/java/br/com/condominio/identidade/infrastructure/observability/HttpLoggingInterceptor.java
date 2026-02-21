package br.com.condominio.identidade.infrastructure.observability;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class HttpLoggingInterceptor implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(HttpLoggingInterceptor.class);
  private static final String START_TIME_ATTRIBUTE = "http.logging.startNanos";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime());
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
}

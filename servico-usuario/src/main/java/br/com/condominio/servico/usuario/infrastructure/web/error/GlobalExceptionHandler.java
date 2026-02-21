package br.com.condominio.servico.usuario.infrastructure.web.error;

import br.com.condominio.servico.usuario.application.exception.ConflictException;
import br.com.condominio.servico.usuario.application.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(
      IllegalArgumentException exception,
      HttpServletRequest request
  ) {
    logWarnWithContext(HttpStatus.BAD_REQUEST, request.getRequestURI(), exception);
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException exception,
      HttpServletRequest request
  ) {
    logWarnWithContext(HttpStatus.BAD_REQUEST, request.getRequestURI(), exception);
    return buildResponse(HttpStatus.BAD_REQUEST, "Dados invalidos", request.getRequestURI());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      NotFoundException exception,
      HttpServletRequest request
  ) {
    logWarnWithContext(HttpStatus.NOT_FOUND, request.getRequestURI(), exception);
    return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(
      ConflictException exception,
      HttpServletRequest request
  ) {
    logWarnWithContext(HttpStatus.CONFLICT, request.getRequestURI(), exception);
    return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleInternalError(
      Exception exception,
      HttpServletRequest request
  ) {
    logErrorWithContext(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI(), exception);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", request.getRequestURI());
  }

  private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, String path) {
    ErrorResponse errorResponse = new ErrorResponse(
        OffsetDateTime.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        path
    );
    return ResponseEntity.status(status).body(errorResponse);
  }

  private void logWarnWithContext(HttpStatus status, String path, Exception exception) {
    try (MDC.MDCCloseable eventType = MDC.putCloseable("event_type", "error");
         MDC.MDCCloseable statusCode = MDC.putCloseable("http.status_code", String.valueOf(status.value()));
         MDC.MDCCloseable requestPath = MDC.putCloseable("http.path", path);
         MDC.MDCCloseable errorType = MDC.putCloseable("error.type", exception.getClass().getSimpleName())) {
      log.warn("request failed with business/client exception: {}", exception.getMessage());
    }
  }

  private void logErrorWithContext(HttpStatus status, String path, Exception exception) {
    try (MDC.MDCCloseable eventType = MDC.putCloseable("event_type", "error");
         MDC.MDCCloseable statusCode = MDC.putCloseable("http.status_code", String.valueOf(status.value()));
         MDC.MDCCloseable requestPath = MDC.putCloseable("http.path", path);
         MDC.MDCCloseable errorType = MDC.putCloseable("error.type", exception.getClass().getSimpleName())) {
      log.error("request failed with internal error", exception);
    }
  }
}

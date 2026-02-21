package br.com.condominio.servico.usuario.infrastructure.web.error;

import br.com.condominio.servico.usuario.application.exception.ConflictException;
import br.com.condominio.servico.usuario.application.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(
      IllegalArgumentException exception,
      HttpServletRequest request
  ) {
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException exception,
      HttpServletRequest request
  ) {
    return buildResponse(HttpStatus.BAD_REQUEST, "Dados invalidos", request.getRequestURI());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      NotFoundException exception,
      HttpServletRequest request
  ) {
    return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(
      ConflictException exception,
      HttpServletRequest request
  ) {
    return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleInternalError(
      Exception exception,
      HttpServletRequest request
  ) {
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
}

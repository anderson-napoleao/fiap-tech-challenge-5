package br.com.condominio.servico.usuario.application.exception;

/**
 * Representa erro de aplicacao para fluxos de negocio.
 */
public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}

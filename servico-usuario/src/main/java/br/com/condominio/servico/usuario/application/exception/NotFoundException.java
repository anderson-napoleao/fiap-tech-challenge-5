package br.com.condominio.servico.usuario.application.exception;

/**
 * Representa erro de aplicacao para fluxos de negocio.
 */
public class NotFoundException extends RuntimeException {

  public NotFoundException(String message) {
    super(message);
  }
}

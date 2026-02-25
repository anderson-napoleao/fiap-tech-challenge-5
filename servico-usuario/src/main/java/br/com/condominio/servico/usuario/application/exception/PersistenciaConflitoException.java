package br.com.condominio.servico.usuario.application.exception;

/**
 * Representa erro de aplicacao para fluxos de negocio.
 */
public class PersistenciaConflitoException extends RuntimeException {

  public PersistenciaConflitoException(String message, Throwable cause) {
    super(message, cause);
  }
}

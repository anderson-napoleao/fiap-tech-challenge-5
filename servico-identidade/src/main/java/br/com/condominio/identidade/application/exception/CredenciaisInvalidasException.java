package br.com.condominio.identidade.application.exception;

/**
 * Representa erro de aplicacao para fluxos de negocio.
 */
public class CredenciaisInvalidasException extends RuntimeException {

  public CredenciaisInvalidasException() {
    super("credenciais invalidas");
  }
}

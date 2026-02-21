package br.com.condominio.identidade.application.exception;

public class CredenciaisInvalidasException extends RuntimeException {

  public CredenciaisInvalidasException() {
    super("credenciais invalidas");
  }
}

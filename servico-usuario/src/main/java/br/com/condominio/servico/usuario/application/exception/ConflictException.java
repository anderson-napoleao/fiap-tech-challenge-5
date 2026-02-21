package br.com.condominio.servico.usuario.application.exception;

public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}

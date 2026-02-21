package br.com.condominio.servico.usuario.infrastructure.web.error;

public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}

package br.com.condominio.servico.usuario.infrastructure.web.error;

public class NotFoundException extends RuntimeException {

  public NotFoundException(String message) {
    super(message);
  }
}

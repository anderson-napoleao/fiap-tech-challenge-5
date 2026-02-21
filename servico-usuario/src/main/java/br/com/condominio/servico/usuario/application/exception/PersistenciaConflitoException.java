package br.com.condominio.servico.usuario.application.exception;

public class PersistenciaConflitoException extends RuntimeException {

  public PersistenciaConflitoException(String message, Throwable cause) {
    super(message, cause);
  }
}

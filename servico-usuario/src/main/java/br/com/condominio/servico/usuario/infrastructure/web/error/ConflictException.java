package br.com.condominio.servico.usuario.infrastructure.web.error;

/**
 * Padroniza tratamento e resposta de erros HTTP.
 */
public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}

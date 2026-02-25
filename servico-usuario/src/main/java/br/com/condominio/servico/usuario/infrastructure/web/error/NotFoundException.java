package br.com.condominio.servico.usuario.infrastructure.web.error;

/**
 * Padroniza tratamento e resposta de erros HTTP.
 */
public class NotFoundException extends RuntimeException {

  public NotFoundException(String message) {
    super(message);
  }
}

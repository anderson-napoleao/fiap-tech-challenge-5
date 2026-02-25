package br.com.condominio.servico.notificacao.application.exception;

/**
 * Representa erro de aplicacao para fluxos de negocio.
 */
public class AcessoNegadoException extends RuntimeException {

  public AcessoNegadoException(String message) {
    super(message);
  }
}

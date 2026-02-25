package br.com.condominio.servico.notificacao.application.exception;

/**
 * Representa erro de aplicacao para fluxos de negocio.
 */
public class NotificacaoNaoEncontradaException extends RuntimeException {

  public NotificacaoNaoEncontradaException(String message) {
    super(message);
  }
}

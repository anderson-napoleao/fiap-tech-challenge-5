package br.com.condominio.servico.encomenda.application.exception;

/**
 * Representa erro de aplicacao para fluxos de negocio.
 */
public class EncomendaNaoEncontradaException extends RuntimeException {

  public EncomendaNaoEncontradaException(String message) {
    super(message);
  }
}

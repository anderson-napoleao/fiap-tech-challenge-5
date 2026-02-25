package br.com.condominio.servico.encomenda.application.exception;

public class EncomendaNaoEncontradaException extends RuntimeException {

  public EncomendaNaoEncontradaException(String message) {
    super(message);
  }
}

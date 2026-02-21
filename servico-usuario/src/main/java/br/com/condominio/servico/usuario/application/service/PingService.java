package br.com.condominio.servico.usuario.application.service;

import br.com.condominio.servico.usuario.application.port.in.PingUseCase;

public class PingService implements PingUseCase {

  @Override
  public String ping() {
    return "pong";
  }
}

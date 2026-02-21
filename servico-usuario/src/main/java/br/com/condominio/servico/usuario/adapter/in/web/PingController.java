package br.com.condominio.servico.usuario.adapter.in.web;

import br.com.condominio.servico.usuario.application.port.in.PingUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

  private final PingUseCase pingUseCase;

  public PingController(PingUseCase pingUseCase) {
    this.pingUseCase = pingUseCase;
  }

  @GetMapping("/ping")
  public String ping() {
    return pingUseCase.ping();
  }
}

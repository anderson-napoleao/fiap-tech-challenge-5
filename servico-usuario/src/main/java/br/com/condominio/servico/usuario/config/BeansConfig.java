package br.com.condominio.servico.usuario.config;

import br.com.condominio.servico.usuario.application.port.in.PingUseCase;
import br.com.condominio.servico.usuario.application.service.PingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

  @Bean
  public PingUseCase pingUseCase() {
    return new PingService();
  }
}

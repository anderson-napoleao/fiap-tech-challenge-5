package br.com.condominio.servico.encomenda.config;

import br.com.condominio.servico.encomenda.application.port.in.ReceberEncomendaUseCase;
import br.com.condominio.servico.encomenda.application.port.out.RegistrarRecebimentoComOutboxPort;
import br.com.condominio.servico.encomenda.application.service.ReceberEncomendaService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

  @Bean
  public Clock appClock() {
    return Clock.systemUTC();
  }

  @Bean
  public ReceberEncomendaUseCase receberEncomendaUseCase(
      RegistrarRecebimentoComOutboxPort registrarRecebimentoComOutboxPort,
      Clock appClock
  ) {
    return new ReceberEncomendaService(registrarRecebimentoComOutboxPort, appClock);
  }
}

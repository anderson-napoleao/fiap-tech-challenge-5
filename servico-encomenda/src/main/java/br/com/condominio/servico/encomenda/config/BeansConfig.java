package br.com.condominio.servico.encomenda.config;

import br.com.condominio.servico.encomenda.application.port.in.BaixarEncomendaRetiradaUseCase;
import br.com.condominio.servico.encomenda.application.port.in.BuscarEncomendaPorIdUseCase;
import br.com.condominio.servico.encomenda.application.port.in.ListarEncomendasPortariaUseCase;
import br.com.condominio.servico.encomenda.application.port.in.ReceberEncomendaUseCase;
import br.com.condominio.servico.encomenda.application.port.out.EncomendaRepositoryPort;
import br.com.condominio.servico.encomenda.application.port.out.RegistrarRecebimentoComOutboxPort;
import br.com.condominio.servico.encomenda.application.service.BaixarEncomendaRetiradaService;
import br.com.condominio.servico.encomenda.application.service.BuscarEncomendaPorIdService;
import br.com.condominio.servico.encomenda.application.service.ListarEncomendasPortariaService;
import br.com.condominio.servico.encomenda.application.service.ReceberEncomendaService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura a composicao de beans e dependencias do modulo.
 */
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

  @Bean
  public BaixarEncomendaRetiradaUseCase baixarEncomendaRetiradaUseCase(
      EncomendaRepositoryPort encomendaRepositoryPort,
      Clock appClock
  ) {
    return new BaixarEncomendaRetiradaService(encomendaRepositoryPort, appClock);
  }

  @Bean
  public BuscarEncomendaPorIdUseCase buscarEncomendaPorIdUseCase(
      EncomendaRepositoryPort encomendaRepositoryPort
  ) {
    return new BuscarEncomendaPorIdService(encomendaRepositoryPort);
  }

  @Bean
  public ListarEncomendasPortariaUseCase listarEncomendasPortariaUseCase(
      EncomendaRepositoryPort encomendaRepositoryPort
  ) {
    return new ListarEncomendasPortariaService(encomendaRepositoryPort);
  }
}

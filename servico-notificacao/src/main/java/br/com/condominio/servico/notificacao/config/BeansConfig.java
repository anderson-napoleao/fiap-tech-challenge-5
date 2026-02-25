package br.com.condominio.servico.notificacao.config;

import br.com.condominio.servico.notificacao.application.port.in.ConfirmarRecebimentoNotificacaoUseCase;
import br.com.condominio.servico.notificacao.application.port.in.ListarNotificacoesPendentesUseCase;
import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaUseCase;
import br.com.condominio.servico.notificacao.application.port.out.NotificacaoRepositoryPort;
import br.com.condominio.servico.notificacao.application.port.out.RegistrarNotificacaoComOutboxPort;
import br.com.condominio.servico.notificacao.application.service.ConfirmarRecebimentoNotificacaoService;
import br.com.condominio.servico.notificacao.application.service.ListarNotificacoesPendentesService;
import br.com.condominio.servico.notificacao.application.service.ProcessarEncomendaRecebidaService;
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
  public ProcessarEncomendaRecebidaUseCase processarEncomendaRecebidaUseCase(
      RegistrarNotificacaoComOutboxPort registrarNotificacaoComOutboxPort,
      Clock appClock
  ) {
    return new ProcessarEncomendaRecebidaService(registrarNotificacaoComOutboxPort, appClock);
  }

  @Bean
  public ConfirmarRecebimentoNotificacaoUseCase confirmarRecebimentoNotificacaoUseCase(
      NotificacaoRepositoryPort notificacaoRepositoryPort,
      Clock appClock
  ) {
    return new ConfirmarRecebimentoNotificacaoService(notificacaoRepositoryPort, appClock);
  }

  @Bean
  public ListarNotificacoesPendentesUseCase listarNotificacoesPendentesUseCase(
      NotificacaoRepositoryPort notificacaoRepositoryPort
  ) {
    return new ListarNotificacoesPendentesService(notificacaoRepositoryPort);
  }
}

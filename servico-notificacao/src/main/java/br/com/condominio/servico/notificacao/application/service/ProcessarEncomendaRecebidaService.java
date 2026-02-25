package br.com.condominio.servico.notificacao.application.service;

import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaUseCase;
import br.com.condominio.servico.notificacao.application.port.out.RegistrarNotificacaoComOutboxPort;
import br.com.condominio.servico.notificacao.domain.Notificacao;
import java.time.Clock;
import java.util.Objects;

/**
 * Implementa a orquestracao de regras da camada de aplicacao.
 */
public class ProcessarEncomendaRecebidaService implements ProcessarEncomendaRecebidaUseCase {

  private final RegistrarNotificacaoComOutboxPort registrarNotificacaoComOutboxPort;
  private final Clock clock;

  public ProcessarEncomendaRecebidaService(
      RegistrarNotificacaoComOutboxPort registrarNotificacaoComOutboxPort,
      Clock clock
  ) {
    this.registrarNotificacaoComOutboxPort =
        Objects.requireNonNull(registrarNotificacaoComOutboxPort, "Porta de notificacao obrigatoria");
    this.clock = Objects.requireNonNull(clock, "Clock obrigatorio");
  }

  @Override
  public Result executar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("Comando obrigatorio");
    }

    Notificacao notificacao = Notificacao.criar(
        command.encomendaId(),
        command.moradorId(),
        command.canal(),
        command.destino(),
        command.mensagem(),
        command.sourceEventId(),
        command.correlationId(),
        clock.instant()
    );
    notificacao.marcarEnviada(clock.instant());

    Notificacao salva = registrarNotificacaoComOutboxPort.registrar(notificacao);

    return new Result(
        salva.id(),
        salva.encomendaId(),
        salva.moradorId(),
        salva.canal(),
        salva.destino(),
        salva.mensagem(),
        salva.status(),
        salva.sourceEventId(),
        salva.correlationId(),
        salva.criadaEm()
    );
  }
}

package br.com.condominio.servico.notificacao.application.service;

import br.com.condominio.servico.notificacao.application.exception.AcessoNegadoException;
import br.com.condominio.servico.notificacao.application.exception.NotificacaoNaoEncontradaException;
import br.com.condominio.servico.notificacao.application.port.in.ConfirmarRecebimentoNotificacaoUseCase;
import br.com.condominio.servico.notificacao.application.port.out.NotificacaoRepositoryPort;
import br.com.condominio.servico.notificacao.domain.Notificacao;
import java.time.Clock;
import java.util.Objects;

/**
 * Implementa a orquestracao de regras da camada de aplicacao.
 */
public class ConfirmarRecebimentoNotificacaoService implements ConfirmarRecebimentoNotificacaoUseCase {

  private final NotificacaoRepositoryPort notificacaoRepositoryPort;
  private final Clock clock;

  public ConfirmarRecebimentoNotificacaoService(
      NotificacaoRepositoryPort notificacaoRepositoryPort,
      Clock clock
  ) {
    this.notificacaoRepositoryPort =
        Objects.requireNonNull(notificacaoRepositoryPort, "Porta de notificacao obrigatoria");
    this.clock = Objects.requireNonNull(clock, "Clock obrigatorio");
  }

  @Override
  public Result executar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("Comando obrigatorio");
    }

    Notificacao notificacao = notificacaoRepositoryPort.buscarPorId(command.notificacaoId())
        .orElseThrow(() -> new NotificacaoNaoEncontradaException("Notificacao nao encontrada"));

    if (!notificacao.moradorId().equals(command.moradorId())) {
      throw new AcessoNegadoException("Notificacao nao pertence ao morador autenticado");
    }

    notificacao.confirmarRecebimento(command.moradorId(), clock.instant());
    Notificacao atualizada = notificacaoRepositoryPort.salvar(notificacao);

    return new Result(
        atualizada.id(),
        atualizada.status(),
        atualizada.confirmadaEm()
    );
  }
}

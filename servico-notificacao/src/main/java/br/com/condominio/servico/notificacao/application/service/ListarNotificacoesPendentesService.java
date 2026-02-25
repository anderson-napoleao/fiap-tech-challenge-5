package br.com.condominio.servico.notificacao.application.service;

import br.com.condominio.servico.notificacao.application.port.in.ListarNotificacoesPendentesUseCase;
import br.com.condominio.servico.notificacao.application.port.out.NotificacaoRepositoryPort;
import br.com.condominio.servico.notificacao.domain.Notificacao;
import java.util.List;
import java.util.Objects;

public class ListarNotificacoesPendentesService implements ListarNotificacoesPendentesUseCase {

  private final NotificacaoRepositoryPort notificacaoRepositoryPort;

  public ListarNotificacoesPendentesService(NotificacaoRepositoryPort notificacaoRepositoryPort) {
    this.notificacaoRepositoryPort =
        Objects.requireNonNull(notificacaoRepositoryPort, "Porta de notificacao obrigatoria");
  }

  @Override
  public Result executar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("Comando obrigatorio");
    }

    List<Notificacao> notificacoes = notificacaoRepositoryPort.listarNaoConfirmadasPorMorador(
        command.moradorId(),
        command.page(),
        command.size()
    );

    List<Item> itens = notificacoes.stream()
        .map(this::toItem)
        .toList();

    return new Result(itens, command.page(), command.size());
  }

  private Item toItem(Notificacao notificacao) {
    return new Item(
        notificacao.id(),
        notificacao.encomendaId(),
        notificacao.canal(),
        notificacao.destino(),
        notificacao.mensagem(),
        notificacao.status(),
        notificacao.criadaEm(),
        notificacao.enviadaEm()
    );
  }
}

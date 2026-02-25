package br.com.condominio.servico.notificacao.application.service;

import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaPorUnidadeUseCase;
import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaUseCase;
import br.com.condominio.servico.notificacao.application.port.out.MoradorDirectoryPort;
import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import java.util.List;
import java.util.Objects;

/**
 * Implementa a orquestracao de regras da camada de aplicacao.
 */
public class ProcessarEncomendaRecebidaPorUnidadeService implements ProcessarEncomendaRecebidaPorUnidadeUseCase {

  private final MoradorDirectoryPort moradorDirectoryPort;
  private final ProcessarEncomendaRecebidaUseCase processarEncomendaRecebidaUseCase;

  public ProcessarEncomendaRecebidaPorUnidadeService(
      MoradorDirectoryPort moradorDirectoryPort,
      ProcessarEncomendaRecebidaUseCase processarEncomendaRecebidaUseCase
  ) {
    this.moradorDirectoryPort = Objects.requireNonNull(moradorDirectoryPort, "Diretorio de moradores obrigatorio");
    this.processarEncomendaRecebidaUseCase =
        Objects.requireNonNull(processarEncomendaRecebidaUseCase, "Use case de notificacao obrigatorio");
  }

  @Override
  public Result executar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("Comando obrigatorio");
    }

    List<MoradorDirectoryPort.Morador> moradores = moradorDirectoryPort.listarMoradoresPorUnidade(
        command.bloco(),
        command.apartamento()
    );

    int processadas = 0;
    for (MoradorDirectoryPort.Morador morador : moradores) {
      processarEncomendaRecebidaUseCase.executar(new ProcessarEncomendaRecebidaUseCase.Command(
          String.valueOf(command.encomendaId()),
          morador.identityId(),
          CanalNotificacao.PUSH,
          morador.identityId(),
          montarMensagem(command),
          command.eventId(),
          command.eventId()
      ));
      processadas++;
    }

    return new Result(command.encomendaId(), moradores.size(), processadas);
  }

  private String montarMensagem(Command command) {
    return "Encomenda recebida para " + command.nomeDestinatario()
        + " na unidade " + command.bloco() + "-" + command.apartamento()
        + ". Descricao: " + command.descricao();
  }
}

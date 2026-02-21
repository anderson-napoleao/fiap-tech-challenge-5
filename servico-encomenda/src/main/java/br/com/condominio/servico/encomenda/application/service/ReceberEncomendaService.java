package br.com.condominio.servico.encomenda.application.service;

import br.com.condominio.servico.encomenda.application.port.in.ReceberEncomendaUseCase;
import br.com.condominio.servico.encomenda.application.port.out.RegistrarRecebimentoComOutboxPort;
import br.com.condominio.servico.encomenda.domain.Encomenda;
import java.time.Clock;
import java.util.Objects;

public class ReceberEncomendaService implements ReceberEncomendaUseCase {

  private final RegistrarRecebimentoComOutboxPort registrarRecebimentoComOutboxPort;
  private final Clock clock;

  public ReceberEncomendaService(
      RegistrarRecebimentoComOutboxPort registrarRecebimentoComOutboxPort,
      Clock clock
  ) {
    this.registrarRecebimentoComOutboxPort =
        Objects.requireNonNull(registrarRecebimentoComOutboxPort, "Porta de registro obrigatoria");
    this.clock = Objects.requireNonNull(clock, "Clock obrigatorio");
  }

  @Override
  public Result executar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("Comando obrigatorio");
    }

    Encomenda recebida = Encomenda.receber(
        command.nomeDestinatario(),
        command.apartamento(),
        command.bloco(),
        command.descricao(),
        command.recebidoPor(),
        clock.instant()
    );

    Encomenda salva = registrarRecebimentoComOutboxPort.registrar(recebida);

    return new Result(
        salva.id(),
        salva.nomeDestinatario(),
        salva.apartamento(),
        salva.bloco(),
        salva.descricao(),
        salva.recebidoPor(),
        salva.status(),
        salva.dataRecebimento()
    );
  }
}

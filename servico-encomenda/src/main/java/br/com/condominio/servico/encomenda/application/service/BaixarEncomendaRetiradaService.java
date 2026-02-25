package br.com.condominio.servico.encomenda.application.service;

import br.com.condominio.servico.encomenda.application.exception.EncomendaNaoEncontradaException;
import br.com.condominio.servico.encomenda.application.port.in.BaixarEncomendaRetiradaUseCase;
import br.com.condominio.servico.encomenda.application.port.out.EncomendaRepositoryPort;
import br.com.condominio.servico.encomenda.domain.Encomenda;
import java.time.Clock;
import java.util.Objects;

/**
 * Implementa a orquestracao de regras da camada de aplicacao.
 */
public class BaixarEncomendaRetiradaService implements BaixarEncomendaRetiradaUseCase {

  private final EncomendaRepositoryPort encomendaRepositoryPort;
  private final Clock clock;

  public BaixarEncomendaRetiradaService(
      EncomendaRepositoryPort encomendaRepositoryPort,
      Clock clock
  ) {
    this.encomendaRepositoryPort =
        Objects.requireNonNull(encomendaRepositoryPort, "Repositorio de encomenda obrigatorio");
    this.clock = Objects.requireNonNull(clock, "Clock obrigatorio");
  }

  @Override
  public Result executar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("Comando obrigatorio");
    }

    Encomenda encomenda = encomendaRepositoryPort.buscarPorId(command.encomendaId())
        .orElseThrow(() -> new EncomendaNaoEncontradaException("Encomenda nao encontrada"));

    encomenda.marcarRetirada(clock.instant(), command.retiradoPorNome());
    Encomenda salva = encomendaRepositoryPort.salvar(encomenda);

    return new Result(
        salva.id(),
        salva.status(),
        salva.dataRetirada(),
        salva.retiradoPorNome()
    );
  }
}

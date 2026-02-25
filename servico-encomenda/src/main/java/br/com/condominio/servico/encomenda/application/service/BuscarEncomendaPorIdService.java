package br.com.condominio.servico.encomenda.application.service;

import br.com.condominio.servico.encomenda.application.exception.EncomendaNaoEncontradaException;
import br.com.condominio.servico.encomenda.application.port.in.BuscarEncomendaPorIdUseCase;
import br.com.condominio.servico.encomenda.application.port.out.EncomendaRepositoryPort;
import br.com.condominio.servico.encomenda.domain.Encomenda;
import java.util.Objects;

/**
 * Implementa a orquestracao de regras da camada de aplicacao.
 */
public class BuscarEncomendaPorIdService implements BuscarEncomendaPorIdUseCase {

  private final EncomendaRepositoryPort encomendaRepositoryPort;

  public BuscarEncomendaPorIdService(EncomendaRepositoryPort encomendaRepositoryPort) {
    this.encomendaRepositoryPort =
        Objects.requireNonNull(encomendaRepositoryPort, "Repositorio de encomenda obrigatorio");
  }

  @Override
  public Result executar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("Comando obrigatorio");
    }

    Encomenda encomenda = encomendaRepositoryPort.buscarPorId(command.encomendaId())
        .orElseThrow(() -> new EncomendaNaoEncontradaException("Encomenda nao encontrada"));

    return new Result(
        encomenda.id(),
        encomenda.nomeDestinatario(),
        encomenda.apartamento(),
        encomenda.bloco(),
        encomenda.descricao(),
        encomenda.recebidoPor(),
        encomenda.status(),
        encomenda.dataRecebimento()
    );
  }
}

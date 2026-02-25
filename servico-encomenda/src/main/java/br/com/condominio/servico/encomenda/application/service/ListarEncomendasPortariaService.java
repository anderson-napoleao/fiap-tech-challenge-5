package br.com.condominio.servico.encomenda.application.service;

import br.com.condominio.servico.encomenda.application.port.in.ListarEncomendasPortariaUseCase;
import br.com.condominio.servico.encomenda.application.port.out.EncomendaRepositoryPort;
import br.com.condominio.servico.encomenda.domain.Encomenda;
import java.util.List;
import java.util.Objects;

/**
 * Implementa a orquestracao de regras da camada de aplicacao.
 */
public class ListarEncomendasPortariaService implements ListarEncomendasPortariaUseCase {

  private final EncomendaRepositoryPort encomendaRepositoryPort;

  public ListarEncomendasPortariaService(EncomendaRepositoryPort encomendaRepositoryPort) {
    this.encomendaRepositoryPort =
        Objects.requireNonNull(encomendaRepositoryPort, "Repositorio de encomenda obrigatorio");
  }

  @Override
  public Result executar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("Comando obrigatorio");
    }

    EncomendaRepositoryPort.ResultadoListagem resultado = encomendaRepositoryPort.listar(
        new EncomendaRepositoryPort.FiltroListagem(
            command.apartamento(),
            command.bloco(),
            command.data(),
            command.page(),
            command.size()
        )
    );

    List<Item> itens = resultado.encomendas().stream()
        .map(this::toItem)
        .toList();

    return new Result(
        itens,
        command.page(),
        command.size(),
        resultado.totalElements(),
        resultado.totalPages()
    );
  }

  private Item toItem(Encomenda encomenda) {
    return new Item(
        encomenda.id(),
        encomenda.nomeDestinatario(),
        encomenda.apartamento(),
        encomenda.bloco(),
        encomenda.descricao(),
        encomenda.recebidoPor(),
        encomenda.status(),
        encomenda.dataRecebimento(),
        encomenda.dataRetirada(),
        encomenda.retiradoPorNome()
    );
  }
}

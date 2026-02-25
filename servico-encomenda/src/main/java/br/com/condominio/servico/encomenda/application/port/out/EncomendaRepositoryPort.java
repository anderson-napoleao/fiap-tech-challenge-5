package br.com.condominio.servico.encomenda.application.port.out;

import br.com.condominio.servico.encomenda.domain.Encomenda;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Define o contrato de saida da aplicacao para integracoes externas.
 */
public interface EncomendaRepositoryPort {

  record FiltroListagem(
      String apartamento,
      String bloco,
      LocalDate data,
      int page,
      int size
  ) {
  }

  record ResultadoListagem(
      List<Encomenda> encomendas,
      long totalElements,
      int totalPages
  ) {
  }

  Optional<Encomenda> buscarPorId(Long id);

  Encomenda salvar(Encomenda encomenda);

  ResultadoListagem listar(FiltroListagem filtro);
}

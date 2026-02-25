package br.com.condominio.servico.encomenda.application.port.in;

import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import java.time.Instant;

/**
 * Define o contrato de entrada (use case) da aplicacao.
 */
public interface BuscarEncomendaPorIdUseCase {

  record Command(Long encomendaId) {
    public Command {
      if (encomendaId == null || encomendaId <= 0) {
        throw new IllegalArgumentException("Encomenda obrigatoria");
      }
    }
  }

  record Result(
      Long id,
      String nomeDestinatario,
      String apartamento,
      String bloco,
      String descricao,
      String recebidoPor,
      StatusEncomenda status,
      Instant dataRecebimento,
      Instant dataRetirada,
      String retiradoPorNome
  ) {
  }

  Result executar(Command command);
}

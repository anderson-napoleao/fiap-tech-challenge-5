package br.com.condominio.servico.encomenda.application.port.in;

import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Define o contrato de entrada (use case) da aplicacao.
 */
public interface ListarEncomendasPortariaUseCase {

  record Command(
      String apartamento,
      String bloco,
      LocalDate data,
      int page,
      int size
  ) {
    public Command {
      if (page < 0) {
        throw new IllegalArgumentException("Page invalida");
      }
      if (size <= 0 || size > 100) {
        throw new IllegalArgumentException("Size invalido");
      }

      apartamento = normalizarOpcional(apartamento);
      bloco = normalizarOpcional(bloco);
    }

    private static String normalizarOpcional(String valor) {
      if (valor == null) {
        return null;
      }
      String normalizado = valor.trim();
      return normalizado.isEmpty() ? null : normalizado;
    }
  }

  record Item(
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

  record Result(
      List<Item> encomendas,
      int page,
      int size,
      long totalElements,
      int totalPages
  ) {
  }

  Result executar(Command command);
}

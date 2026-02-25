package br.com.condominio.servico.encomenda.application.port.in;

import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import java.time.Instant;

/**
 * Define o contrato de entrada (use case) da aplicacao.
 */
public interface BaixarEncomendaRetiradaUseCase {

  record Command(
      Long encomendaId,
      String retiradoPorNome
  ) {
    public Command {
      if (encomendaId == null || encomendaId <= 0) {
        throw new IllegalArgumentException("Encomenda obrigatoria");
      }
      validarObrigatorio(retiradoPorNome, "Nome de quem retirou obrigatorio");
    }

    private static void validarObrigatorio(String valor, String mensagem) {
      if (valor == null || valor.isBlank()) {
        throw new IllegalArgumentException(mensagem);
      }
    }
  }

  record Result(
      Long encomendaId,
      StatusEncomenda status,
      Instant dataRetirada,
      String retiradoPorNome
  ) {
  }

  Result executar(Command command);
}

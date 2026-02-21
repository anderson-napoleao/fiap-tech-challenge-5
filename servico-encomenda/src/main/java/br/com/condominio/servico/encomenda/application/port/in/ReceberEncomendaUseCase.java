package br.com.condominio.servico.encomenda.application.port.in;

import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import java.time.Instant;

public interface ReceberEncomendaUseCase {

  record Command(
      String nomeDestinatario,
      String apartamento,
      String bloco,
      String descricao,
      String recebidoPor
  ) {
    public Command {
      validarObrigatorio(nomeDestinatario, "Nome do destinatario obrigatorio");
      validarObrigatorio(apartamento, "Apartamento obrigatorio");
      validarObrigatorio(bloco, "Bloco obrigatorio");
      validarObrigatorio(descricao, "Descricao obrigatoria");
      validarObrigatorio(recebidoPor, "Recebido por obrigatorio");
    }

    private static void validarObrigatorio(String valor, String mensagem) {
      if (valor == null || valor.isBlank()) {
        throw new IllegalArgumentException(mensagem);
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
      Instant dataRecebimento
  ) {
  }

  Result executar(Command command);
}

package br.com.condominio.servico.notificacao.application.port.in;

/**
 * Define o contrato de entrada (use case) da aplicacao.
 */
public interface ProcessarEncomendaRecebidaPorUnidadeUseCase {

  record Command(
      String eventId,
      long encomendaId,
      String nomeDestinatario,
      String apartamento,
      String bloco,
      String descricao
  ) {
    public Command {
      validarObrigatorio(eventId, "eventId obrigatorio");
      if (encomendaId <= 0) {
        throw new IllegalArgumentException("encomendaId invalido");
      }
      validarObrigatorio(nomeDestinatario, "nomeDestinatario obrigatorio");
      validarObrigatorio(apartamento, "apartamento obrigatorio");
      validarObrigatorio(bloco, "bloco obrigatorio");
      validarObrigatorio(descricao, "descricao obrigatoria");
    }

    private static void validarObrigatorio(String valor, String mensagem) {
      if (valor == null || valor.isBlank()) {
        throw new IllegalArgumentException(mensagem);
      }
    }
  }

  record Result(
      long encomendaId,
      int moradoresEncontrados,
      int notificacoesProcessadas
  ) {
  }

  Result executar(Command command);
}

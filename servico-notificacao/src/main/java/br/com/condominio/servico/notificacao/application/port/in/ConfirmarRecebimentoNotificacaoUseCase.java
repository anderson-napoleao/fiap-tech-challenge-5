package br.com.condominio.servico.notificacao.application.port.in;

import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import java.time.Instant;

/**
 * Define o contrato de entrada (use case) da aplicacao.
 */
public interface ConfirmarRecebimentoNotificacaoUseCase {

  record Command(
      String notificacaoId,
      String moradorId
  ) {
    public Command {
      validarObrigatorio(notificacaoId, "Notificacao obrigatoria");
      validarObrigatorio(moradorId, "Morador obrigatorio");
    }

    private static void validarObrigatorio(String valor, String mensagem) {
      if (valor == null || valor.isBlank()) {
        throw new IllegalArgumentException(mensagem);
      }
    }
  }

  record Result(
      String id,
      StatusNotificacao status,
      Instant confirmadaEm
  ) {
  }

  Result executar(Command command);
}

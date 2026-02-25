package br.com.condominio.servico.notificacao.application.port.in;

import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import java.time.Instant;

public interface ProcessarEncomendaRecebidaUseCase {

  record Command(
      String encomendaId,
      String moradorId,
      CanalNotificacao canal,
      String destino,
      String mensagem,
      String sourceEventId,
      String correlationId
  ) {
    public Command {
      validarObrigatorio(encomendaId, "Encomenda obrigatoria");
      validarObrigatorio(moradorId, "Morador obrigatorio");
      if (canal == null) {
        throw new IllegalArgumentException("Canal obrigatorio");
      }
      validarObrigatorio(destino, "Destino obrigatorio");
      validarObrigatorio(mensagem, "Mensagem obrigatoria");
      validarObrigatorio(sourceEventId, "Source event id obrigatorio");
      validarObrigatorio(correlationId, "Correlation id obrigatorio");
    }

    private static void validarObrigatorio(String valor, String mensagem) {
      if (valor == null || valor.isBlank()) {
        throw new IllegalArgumentException(mensagem);
      }
    }
  }

  record Result(
      String id,
      String encomendaId,
      String moradorId,
      CanalNotificacao canal,
      String destino,
      String mensagem,
      StatusNotificacao status,
      String sourceEventId,
      String correlationId,
      Instant criadaEm
  ) {
  }

  Result executar(Command command);
}

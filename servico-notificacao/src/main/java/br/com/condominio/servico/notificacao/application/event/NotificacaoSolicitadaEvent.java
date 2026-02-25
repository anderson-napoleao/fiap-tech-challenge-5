package br.com.condominio.servico.notificacao.application.event;

import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import java.time.Instant;

public record NotificacaoSolicitadaEvent(
    String eventId,
    int eventVersion,
    Instant occurredAt,
    String notificacaoId,
    String encomendaId,
    String moradorId,
    CanalNotificacao canal,
    String destino,
    String mensagem,
    String correlationId,
    StatusNotificacao status
) {
  public NotificacaoSolicitadaEvent {
    validarObrigatorio(eventId, "eventId obrigatorio");
    if (eventVersion <= 0) {
      throw new IllegalArgumentException("eventVersion invalido");
    }
    validarInstante(occurredAt, "occurredAt obrigatorio");
    validarObrigatorio(notificacaoId, "notificacaoId obrigatorio");
    validarObrigatorio(encomendaId, "encomendaId obrigatorio");
    validarObrigatorio(moradorId, "moradorId obrigatorio");
    if (canal == null) {
      throw new IllegalArgumentException("canal obrigatorio");
    }
    validarObrigatorio(destino, "destino obrigatorio");
    validarObrigatorio(mensagem, "mensagem obrigatoria");
    validarObrigatorio(correlationId, "correlationId obrigatorio");
    if (status == null) {
      throw new IllegalArgumentException("status obrigatorio");
    }
  }

  private static void validarObrigatorio(String valor, String mensagem) {
    if (valor == null || valor.isBlank()) {
      throw new IllegalArgumentException(mensagem);
    }
  }

  private static void validarInstante(Instant valor, String mensagem) {
    if (valor == null) {
      throw new IllegalArgumentException(mensagem);
    }
  }
}

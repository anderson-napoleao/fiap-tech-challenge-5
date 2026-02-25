package br.com.condominio.servico.notificacao.application.event;

import java.time.Instant;

public record NotificacaoFalhouEvent(
    String eventId,
    int eventVersion,
    Instant occurredAt,
    String notificacaoId,
    String encomendaId,
    String moradorId,
    String motivoFalha,
    String correlationId
) {
  public NotificacaoFalhouEvent {
    validarObrigatorio(eventId, "eventId obrigatorio");
    if (eventVersion <= 0) {
      throw new IllegalArgumentException("eventVersion invalido");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt obrigatorio");
    }
    validarObrigatorio(notificacaoId, "notificacaoId obrigatorio");
    validarObrigatorio(encomendaId, "encomendaId obrigatorio");
    validarObrigatorio(moradorId, "moradorId obrigatorio");
    validarObrigatorio(motivoFalha, "motivoFalha obrigatorio");
    validarObrigatorio(correlationId, "correlationId obrigatorio");
  }

  private static void validarObrigatorio(String valor, String mensagem) {
    if (valor == null || valor.isBlank()) {
      throw new IllegalArgumentException(mensagem);
    }
  }
}

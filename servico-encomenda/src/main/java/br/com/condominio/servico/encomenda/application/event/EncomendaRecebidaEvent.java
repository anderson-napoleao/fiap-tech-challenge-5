package br.com.condominio.servico.encomenda.application.event;

import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import java.time.Instant;

public record EncomendaRecebidaEvent(
    String eventId,
    int eventVersion,
    Instant occurredAt,
    Long encomendaId,
    String nomeDestinatario,
    String apartamento,
    String bloco,
    String descricao,
    String recebidoPor,
    StatusEncomenda status
) {
  public EncomendaRecebidaEvent {
    validarObrigatorio(eventId, "eventId obrigatorio");
    if (eventVersion <= 0) {
      throw new IllegalArgumentException("eventVersion invalido");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt obrigatorio");
    }
    if (encomendaId == null || encomendaId <= 0) {
      throw new IllegalArgumentException("encomendaId invalido");
    }
    validarObrigatorio(nomeDestinatario, "nomeDestinatario obrigatorio");
    validarObrigatorio(apartamento, "apartamento obrigatorio");
    validarObrigatorio(bloco, "bloco obrigatorio");
    validarObrigatorio(descricao, "descricao obrigatoria");
    validarObrigatorio(recebidoPor, "recebidoPor obrigatorio");
    if (status == null) {
      throw new IllegalArgumentException("status obrigatorio");
    }
  }

  private static void validarObrigatorio(String valor, String mensagem) {
    if (valor == null || valor.isBlank()) {
      throw new IllegalArgumentException(mensagem);
    }
  }
}

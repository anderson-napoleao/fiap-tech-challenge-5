package br.com.condominio.servico.notificacao.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificacaoEntityTest {

  @Test
  void deveSetarEObterTodosOsCampos() {
    NotificacaoEntity entity = new NotificacaoEntity();
    Instant createdAt = Instant.parse("2026-02-25T10:00:00Z");
    Instant sentAt = Instant.parse("2026-02-25T10:01:00Z");
    Instant confirmedAt = Instant.parse("2026-02-25T10:02:00Z");
    Instant failedAt = Instant.parse("2026-02-25T10:03:00Z");

    entity.setId("not-1");
    entity.setEncomendaId("enc-1");
    entity.setMoradorId("morador-1");
    entity.setCanal(CanalNotificacao.PUSH);
    entity.setDestino("device-1");
    entity.setMensagem("Sua encomenda chegou");
    entity.setStatus(StatusNotificacao.CONFIRMADA);
    entity.setSourceEventId("source-1");
    entity.setCorrelationId("corr-1");
    entity.setCreatedAt(createdAt);
    entity.setSentAt(sentAt);
    entity.setConfirmedAt(confirmedAt);
    entity.setFailedAt(failedAt);
    entity.setFailureReason("timeout");

    assertEquals("not-1", entity.getId());
    assertEquals("enc-1", entity.getEncomendaId());
    assertEquals("morador-1", entity.getMoradorId());
    assertEquals(CanalNotificacao.PUSH, entity.getCanal());
    assertEquals("device-1", entity.getDestino());
    assertEquals("Sua encomenda chegou", entity.getMensagem());
    assertEquals(StatusNotificacao.CONFIRMADA, entity.getStatus());
    assertEquals("source-1", entity.getSourceEventId());
    assertEquals("corr-1", entity.getCorrelationId());
    assertEquals(createdAt, entity.getCreatedAt());
    assertEquals(sentAt, entity.getSentAt());
    assertEquals(confirmedAt, entity.getConfirmedAt());
    assertEquals(failedAt, entity.getFailedAt());
    assertEquals("timeout", entity.getFailureReason());
  }
}

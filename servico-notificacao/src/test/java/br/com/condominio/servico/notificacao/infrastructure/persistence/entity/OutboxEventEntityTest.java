package br.com.condominio.servico.notificacao.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class OutboxEventEntityTest {

  @Test
  void deveSetarEObterTodosOsCampos() {
    OutboxEventEntity entity = new OutboxEventEntity();
    Instant eventTimestamp = Instant.parse("2026-02-25T12:00:00Z");

    entity.setId("evt-1");
    entity.setAggregateType("NOTIFICACAO");
    entity.setAggregateId("not-1");
    entity.setType("NotificacaoSolicitada");
    entity.setEventVersion(1);
    entity.setPayload("{\"event\":\"ok\"}");
    entity.setEventTimestamp(eventTimestamp);
    entity.setEventTimestampMs(eventTimestamp.toEpochMilli());

    assertEquals("evt-1", entity.getId());
    assertEquals("NOTIFICACAO", entity.getAggregateType());
    assertEquals("not-1", entity.getAggregateId());
    assertEquals("NotificacaoSolicitada", entity.getType());
    assertEquals(1, entity.getEventVersion());
    assertEquals("{\"event\":\"ok\"}", entity.getPayload());
    assertEquals(eventTimestamp, entity.getEventTimestamp());
    assertEquals(eventTimestamp.toEpochMilli(), entity.getEventTimestampMs());
  }
}

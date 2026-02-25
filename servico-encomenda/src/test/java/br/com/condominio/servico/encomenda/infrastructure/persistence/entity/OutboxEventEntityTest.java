package br.com.condominio.servico.encomenda.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class OutboxEventEntityTest {

  @Test
  void deveSetarEObterTodosOsCampos() {
    OutboxEventEntity entity = new OutboxEventEntity();
    Instant timestamp = Instant.parse("2026-02-25T10:00:00Z");

    entity.setId("event-1");
    entity.setAggregateType("ENCOMENDA");
    entity.setAggregateId("1");
    entity.setType("EncomendaRecebida");
    entity.setEventVersion(1);
    entity.setPayload("{\"ok\":true}");
    entity.setEventTimestamp(timestamp);
    entity.setEventTimestampMs(timestamp.toEpochMilli());

    assertEquals("event-1", entity.getId());
    assertEquals("ENCOMENDA", entity.getAggregateType());
    assertEquals("1", entity.getAggregateId());
    assertEquals("EncomendaRecebida", entity.getType());
    assertEquals(1, entity.getEventVersion());
    assertEquals("{\"ok\":true}", entity.getPayload());
    assertEquals(timestamp, entity.getEventTimestamp());
    assertEquals(timestamp.toEpochMilli(), entity.getEventTimestampMs());
  }
}


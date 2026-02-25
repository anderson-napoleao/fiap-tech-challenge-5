package br.com.condominio.servico.encomenda.application.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class EncomendaRecebidaEventTest {

  @Test
  void deveCriarEventoValido() {
    EncomendaRecebidaEvent event = new EncomendaRecebidaEvent(
        "event-1",
        1,
        Instant.parse("2026-02-25T10:00:00Z"),
        10L,
        "Maria",
        "101",
        "A",
        "Caixa",
        "porteiro-1",
        StatusEncomenda.RECEBIDA
    );

    assertEquals("event-1", event.eventId());
    assertEquals(10L, event.encomendaId());
  }

  @Test
  void deveValidarCamposObrigatorios() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new EncomendaRecebidaEvent(
            "event-1",
            0,
            Instant.parse("2026-02-25T10:00:00Z"),
            10L,
            "Maria",
            "101",
            "A",
            "Caixa",
            "porteiro-1",
            StatusEncomenda.RECEBIDA
        )
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new EncomendaRecebidaEvent(
            "event-1",
            1,
            Instant.parse("2026-02-25T10:00:00Z"),
            0L,
            "Maria",
            "101",
            "A",
            "Caixa",
            "porteiro-1",
            StatusEncomenda.RECEBIDA
        )
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new EncomendaRecebidaEvent(
            "event-1",
            1,
            Instant.parse("2026-02-25T10:00:00Z"),
            10L,
            "Maria",
            "101",
            "A",
            "Caixa",
            "porteiro-1",
            null
        )
    );
  }
}


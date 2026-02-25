package br.com.condominio.servico.encomenda.adapter.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.encomenda.application.event.EncomendaRecebidaEvent;
import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutboxPayloadMapperTest {

  @Mock
  private ObjectMapper objectMapper;

  @Test
  void deveSerializarPayload() throws Exception {
    OutboxPayloadMapper mapper = new OutboxPayloadMapper(objectMapper);
    EncomendaRecebidaEvent event = new EncomendaRecebidaEvent(
        "event-1",
        1,
        Instant.parse("2026-02-25T10:00:00Z"),
        1L,
        "Maria",
        "101",
        "A",
        "Caixa",
        "porteiro-1",
        StatusEncomenda.RECEBIDA
    );
    when(objectMapper.writeValueAsString(event)).thenReturn("{\"eventId\":\"event-1\"}");

    String json = mapper.toJson(event);

    assertEquals("{\"eventId\":\"event-1\"}", json);
  }

  @Test
  void deveLancarExcecaoQuandoFalharSerializacao() throws Exception {
    OutboxPayloadMapper mapper = new OutboxPayloadMapper(objectMapper);
    EncomendaRecebidaEvent event = new EncomendaRecebidaEvent(
        "event-1",
        1,
        Instant.parse("2026-02-25T10:00:00Z"),
        1L,
        "Maria",
        "101",
        "A",
        "Caixa",
        "porteiro-1",
        StatusEncomenda.RECEBIDA
    );
    when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("erro") {
    });

    assertThrows(IllegalStateException.class, () -> mapper.toJson(event));
  }
}


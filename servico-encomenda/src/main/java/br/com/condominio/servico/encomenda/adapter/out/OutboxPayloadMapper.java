package br.com.condominio.servico.encomenda.adapter.out;

import br.com.condominio.servico.encomenda.application.event.EncomendaRecebidaEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Adaptador de saida para persistencia ou integracao externa.
 */
@Component
public class OutboxPayloadMapper {

  private final ObjectMapper objectMapper;

  public OutboxPayloadMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String toJson(EncomendaRecebidaEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Falha ao serializar payload de outbox", exception);
    }
  }
}

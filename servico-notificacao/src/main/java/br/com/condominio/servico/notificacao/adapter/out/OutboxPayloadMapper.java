package br.com.condominio.servico.notificacao.adapter.out;

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

  public String toJson(Object event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Falha ao serializar payload de outbox", exception);
    }
  }
}

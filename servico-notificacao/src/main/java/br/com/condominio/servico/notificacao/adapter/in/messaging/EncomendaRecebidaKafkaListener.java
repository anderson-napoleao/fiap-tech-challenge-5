package br.com.condominio.servico.notificacao.adapter.in.messaging;

import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaPorUnidadeUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada para consumo de eventos de mensageria.
 */
@Component
public class EncomendaRecebidaKafkaListener {

  private static final Logger log = LoggerFactory.getLogger(EncomendaRecebidaKafkaListener.class);

  private final ObjectMapper objectMapper;
  private final ProcessarEncomendaRecebidaPorUnidadeUseCase processarEncomendaRecebidaPorUnidadeUseCase;

  public EncomendaRecebidaKafkaListener(
      ObjectMapper objectMapper,
      ProcessarEncomendaRecebidaPorUnidadeUseCase processarEncomendaRecebidaPorUnidadeUseCase
  ) {
    this.objectMapper = objectMapper;
    this.processarEncomendaRecebidaPorUnidadeUseCase = processarEncomendaRecebidaPorUnidadeUseCase;
  }

  @KafkaListener(
      topics = "${app.kafka.topics.encomenda-recebida:encomenda.recebida}",
      groupId = "${app.kafka.consumer.group-id:servico-notificacao}"
  )
  public void consumir(String payload) {
    EncomendaRecebidaMessage message = parse(payload);
    log.info("processing package event {} for apartment {}-{}", message.eventId(), message.bloco(), message.apartamento());

    ProcessarEncomendaRecebidaPorUnidadeUseCase.Result result =
        processarEncomendaRecebidaPorUnidadeUseCase.executar(
            new ProcessarEncomendaRecebidaPorUnidadeUseCase.Command(
                message.eventId(),
                message.encomendaId(),
                message.nomeDestinatario(),
                message.apartamento(),
                message.bloco(),
                message.descricao()
            )
        );

    log.info(
        "package event {} processed with {} residents and {} notifications",
        message.eventId(),
        result.moradoresEncontrados(),
        result.notificacoesProcessadas()
    );
  }

  private EncomendaRecebidaMessage parse(String payload) {
    try {
      JsonNode root = objectMapper.readTree(payload);
      JsonNode content = root.hasNonNull("payload") ? root.get("payload") : root;

      if (content.isTextual()) {
        return objectMapper.readValue(content.asText(), EncomendaRecebidaMessage.class);
      }
      return objectMapper.treeToValue(content, EncomendaRecebidaMessage.class);
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("Falha ao desserializar evento encomenda.recebida", exception);
    }
  }
}

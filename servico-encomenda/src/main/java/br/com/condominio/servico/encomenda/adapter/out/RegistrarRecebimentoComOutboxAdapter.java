package br.com.condominio.servico.encomenda.adapter.out;

import br.com.condominio.servico.encomenda.application.event.EncomendaRecebidaEvent;
import br.com.condominio.servico.encomenda.application.port.out.RegistrarRecebimentoComOutboxPort;
import br.com.condominio.servico.encomenda.domain.Encomenda;
import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.EncomendaEntity;
import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.OutboxEventEntity;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataEncomendaRepository;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegistrarRecebimentoComOutboxAdapter implements RegistrarRecebimentoComOutboxPort {

  private static final String AGGREGATE_TYPE_ENCOMENDA = "ENCOMENDA";
  private static final String EVENT_TYPE_ENCOMENDA_RECEBIDA = "EncomendaRecebida";
  private static final int EVENT_VERSION = 1;

  private final SpringDataEncomendaRepository encomendaRepository;
  private final SpringDataOutboxEventRepository outboxEventRepository;
  private final OutboxPayloadMapper outboxPayloadMapper;
  private final Clock clock;

  public RegistrarRecebimentoComOutboxAdapter(
      SpringDataEncomendaRepository encomendaRepository,
      SpringDataOutboxEventRepository outboxEventRepository,
      OutboxPayloadMapper outboxPayloadMapper,
      Clock clock
  ) {
    this.encomendaRepository = encomendaRepository;
    this.outboxEventRepository = outboxEventRepository;
    this.outboxPayloadMapper = outboxPayloadMapper;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Encomenda registrar(Encomenda encomenda) {
    if (encomenda == null) {
      throw new IllegalArgumentException("Encomenda obrigatoria");
    }

    EncomendaEntity salva = encomendaRepository.save(toEntity(encomenda));
    Encomenda encomendaSalva = toDomain(salva);

    EncomendaRecebidaEvent event = new EncomendaRecebidaEvent(
        UUID.randomUUID().toString(),
        EVENT_VERSION,
        clock.instant(),
        encomendaSalva.id(),
        encomendaSalva.nomeDestinatario(),
        encomendaSalva.apartamento(),
        encomendaSalva.bloco(),
        encomendaSalva.descricao(),
        encomendaSalva.recebidoPor(),
        encomendaSalva.status()
    );

    OutboxEventEntity outboxEvent = new OutboxEventEntity();
    outboxEvent.setId(event.eventId());
    outboxEvent.setAggregateType(AGGREGATE_TYPE_ENCOMENDA);
    outboxEvent.setAggregateId(String.valueOf(event.encomendaId()));
    outboxEvent.setType(EVENT_TYPE_ENCOMENDA_RECEBIDA);
    outboxEvent.setEventVersion(event.eventVersion());
    outboxEvent.setPayload(outboxPayloadMapper.toJson(event));
    outboxEvent.setEventTimestamp(event.occurredAt());
    outboxEvent.setEventTimestampMs(event.occurredAt().toEpochMilli());
    outboxEventRepository.save(outboxEvent);

    return encomendaSalva;
  }

  private EncomendaEntity toEntity(Encomenda encomenda) {
    EncomendaEntity entity = new EncomendaEntity();
    entity.setId(encomenda.id());
    entity.setNomeDestinatario(encomenda.nomeDestinatario());
    entity.setApartamento(encomenda.apartamento());
    entity.setBloco(encomenda.bloco());
    entity.setDescricao(encomenda.descricao());
    entity.setRecebidoPor(encomenda.recebidoPor());
    entity.setStatus(encomenda.status());
    entity.setDataRecebimento(encomenda.dataRecebimento());
    entity.setDataRetirada(encomenda.dataRetirada());
    entity.setRetiradoPorNome(encomenda.retiradoPorNome());
    return entity;
  }

  private Encomenda toDomain(EncomendaEntity entity) {
    Encomenda encomenda = Encomenda.receber(
        entity.getNomeDestinatario(),
        entity.getApartamento(),
        entity.getBloco(),
        entity.getDescricao(),
        entity.getRecebidoPor(),
        entity.getDataRecebimento()
    ).atribuirId(entity.getId());

    if (entity.getStatus() == StatusEncomenda.RETIRADA) {
      Instant dataRetirada = entity.getDataRetirada();
      encomenda.marcarRetirada(dataRetirada, entity.getRetiradoPorNome());
    }

    return encomenda;
  }
}

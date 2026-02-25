package br.com.condominio.servico.notificacao.adapter.out;

import br.com.condominio.servico.notificacao.application.event.NotificacaoSolicitadaEvent;
import br.com.condominio.servico.notificacao.application.port.out.RegistrarNotificacaoComOutboxPort;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import br.com.condominio.servico.notificacao.domain.Notificacao;
import br.com.condominio.servico.notificacao.infrastructure.persistence.entity.NotificacaoEntity;
import br.com.condominio.servico.notificacao.infrastructure.persistence.entity.OutboxEventEntity;
import br.com.condominio.servico.notificacao.infrastructure.persistence.repository.SpringDataNotificacaoRepository;
import br.com.condominio.servico.notificacao.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de saida para persistencia ou integracao externa.
 */
@Component
public class RegistrarNotificacaoComOutboxAdapter implements RegistrarNotificacaoComOutboxPort {

  private static final String AGGREGATE_TYPE_NOTIFICACAO = "NOTIFICACAO";
  private static final String EVENT_TYPE_NOTIFICACAO_SOLICITADA = "NotificacaoSolicitada";
  private static final int EVENT_VERSION = 1;

  private final SpringDataNotificacaoRepository notificacaoRepository;
  private final SpringDataOutboxEventRepository outboxEventRepository;
  private final OutboxPayloadMapper outboxPayloadMapper;
  private final Clock clock;

  public RegistrarNotificacaoComOutboxAdapter(
      SpringDataNotificacaoRepository notificacaoRepository,
      SpringDataOutboxEventRepository outboxEventRepository,
      OutboxPayloadMapper outboxPayloadMapper,
      Clock clock
  ) {
    this.notificacaoRepository = notificacaoRepository;
    this.outboxEventRepository = outboxEventRepository;
    this.outboxPayloadMapper = outboxPayloadMapper;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Notificacao registrar(Notificacao notificacao) {
    if (notificacao == null) {
      throw new IllegalArgumentException("Notificacao obrigatoria");
    }

    // Reprocessamento do mesmo evento de entrada deve ser idempotente.
    NotificacaoEntity existente = notificacaoRepository.findBySourceEventIdAndMoradorId(
        notificacao.sourceEventId(),
        notificacao.moradorId()
    ).orElse(null);
    if (existente != null) {
      return toDomain(existente);
    }

    NotificacaoEntity entity = new NotificacaoEntity();
    String notificacaoId = notificacao.id();
    if (notificacaoId == null || notificacaoId.isBlank()) {
      notificacaoId = UUID.randomUUID().toString();
    }

    entity.setId(notificacaoId);
    entity.setEncomendaId(notificacao.encomendaId());
    entity.setMoradorId(notificacao.moradorId());
    entity.setCanal(notificacao.canal());
    entity.setDestino(notificacao.destino());
    entity.setMensagem(notificacao.mensagem());
    entity.setStatus(notificacao.status());
    entity.setSourceEventId(notificacao.sourceEventId());
    entity.setCorrelationId(notificacao.correlationId());
    entity.setCreatedAt(notificacao.criadaEm());
    entity.setSentAt(notificacao.enviadaEm());
    entity.setConfirmedAt(notificacao.confirmadaEm());
    entity.setFailedAt(notificacao.falhaEm());
    entity.setFailureReason(notificacao.motivoFalha());

    NotificacaoEntity salva;
    try {
      salva = notificacaoRepository.save(entity);
    } catch (DataIntegrityViolationException exception) {
      NotificacaoEntity duplicada = notificacaoRepository.findBySourceEventIdAndMoradorId(
              notificacao.sourceEventId(),
              notificacao.moradorId()
          )
          .orElseThrow(() -> exception);
      return toDomain(duplicada);
    }

    NotificacaoSolicitadaEvent event = new NotificacaoSolicitadaEvent(
        UUID.randomUUID().toString(),
        EVENT_VERSION,
        clock.instant(),
        salva.getId(),
        salva.getEncomendaId(),
        salva.getMoradorId(),
        salva.getCanal(),
        salva.getDestino(),
        salva.getMensagem(),
        salva.getCorrelationId(),
        salva.getStatus()
    );

    OutboxEventEntity outboxEvent = new OutboxEventEntity();
    outboxEvent.setId(event.eventId());
    outboxEvent.setAggregateType(AGGREGATE_TYPE_NOTIFICACAO);
    outboxEvent.setAggregateId(event.notificacaoId());
    outboxEvent.setType(EVENT_TYPE_NOTIFICACAO_SOLICITADA);
    outboxEvent.setEventVersion(event.eventVersion());
    outboxEvent.setPayload(outboxPayloadMapper.toJson(event));
    outboxEvent.setEventTimestamp(event.occurredAt());
    outboxEvent.setEventTimestampMs(event.occurredAt().toEpochMilli());
    outboxEventRepository.save(outboxEvent);

    return notificacao.atribuirId(salva.getId());
  }

  private Notificacao toDomain(NotificacaoEntity entity) {
    Notificacao notificacao = Notificacao.criar(
        entity.getEncomendaId(),
        entity.getMoradorId(),
        entity.getCanal(),
        entity.getDestino(),
        entity.getMensagem(),
        entity.getSourceEventId(),
        entity.getCorrelationId(),
        entity.getCreatedAt()
    ).atribuirId(entity.getId());

    aplicarStatus(notificacao, entity);
    return notificacao;
  }

  private void aplicarStatus(Notificacao notificacao, NotificacaoEntity entity) {
    if (entity.getStatus() == null || entity.getStatus() == StatusNotificacao.PENDENTE) {
      return;
    }

    if (entity.getStatus() == StatusNotificacao.ENVIADA) {
      notificacao.marcarEnviada(valorOuCriadaEm(entity.getSentAt(), entity));
      return;
    }

    if (entity.getStatus() == StatusNotificacao.FALHA) {
      notificacao.marcarFalha(
          valorOuPadrao(entity.getFailureReason(), "Falha de envio"),
          valorOuCriadaEm(entity.getFailedAt(), entity)
      );
      return;
    }

    if (entity.getStatus() == StatusNotificacao.CONFIRMADA) {
      notificacao.marcarEnviada(valorOuCriadaEm(entity.getSentAt(), entity));
      notificacao.confirmarRecebimento(entity.getMoradorId(), valorOuCriadaEm(entity.getConfirmedAt(), entity));
    }
  }

  private Instant valorOuCriadaEm(Instant valor, NotificacaoEntity entity) {
    if (valor != null) {
      return valor;
    }
    if (entity.getCreatedAt() != null) {
      return entity.getCreatedAt();
    }
    throw new IllegalStateException("createdAt obrigatorio para reconstruir notificacao");
  }

  private String valorOuPadrao(String valor, String padrao) {
    return (valor == null || valor.isBlank()) ? padrao : valor;
  }
}

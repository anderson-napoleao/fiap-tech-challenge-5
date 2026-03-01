package br.com.condominio.servico.notificacao.adapter.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.Notificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import br.com.condominio.servico.notificacao.infrastructure.persistence.entity.NotificacaoEntity;
import br.com.condominio.servico.notificacao.infrastructure.persistence.entity.OutboxEventEntity;
import br.com.condominio.servico.notificacao.infrastructure.persistence.repository.SpringDataNotificacaoRepository;
import br.com.condominio.servico.notificacao.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class RegistrarNotificacaoComOutboxAdapterTest {

  @Mock
  private SpringDataNotificacaoRepository notificacaoRepository;

  @Mock
  private SpringDataOutboxEventRepository outboxEventRepository;

  @Mock
  private OutboxPayloadMapper outboxPayloadMapper;

  private RegistrarNotificacaoComOutboxAdapter adapter;

  private final Clock fixedClock = Clock.fixed(Instant.parse("2026-02-25T12:00:00Z"), ZoneOffset.UTC);

  @BeforeEach
  void setup() {
    adapter = new RegistrarNotificacaoComOutboxAdapter(
        notificacaoRepository,
        outboxEventRepository,
        outboxPayloadMapper,
        fixedClock
    );
  }

  @Test
  void deveFalharQuandoNotificacaoForNula() {
    assertThrows(IllegalArgumentException.class, () -> adapter.registrar(null));
  }

  @Test
  void deveRetornarNotificacaoExistenteEmReprocessamentoIdempotente() {
    NotificacaoEntity existente = entityComStatus(StatusNotificacao.ENVIADA);
    existente.setId("not-1");
    existente.setCreatedAt(Instant.parse("2026-02-25T10:00:00Z"));
    existente.setSentAt(Instant.parse("2026-02-25T10:01:00Z"));

    when(notificacaoRepository.findBySourceEventIdAndMoradorId("source-1", "morador-1"))
        .thenReturn(Optional.of(existente));

    Notificacao result = adapter.registrar(notificacaoEnviadaSemId());

    assertEquals("not-1", result.id());
    assertEquals(StatusNotificacao.ENVIADA, result.status());
    verify(notificacaoRepository, never()).save(any(NotificacaoEntity.class));
    verify(outboxEventRepository, never()).save(any(OutboxEventEntity.class));
  }

  @Test
  void devePersistirNotificacaoENovoEventoOutbox() {
    when(notificacaoRepository.findBySourceEventIdAndMoradorId("source-1", "morador-1"))
        .thenReturn(Optional.empty());
    when(notificacaoRepository.save(any(NotificacaoEntity.class)))
        .thenAnswer(invocation -> {
          NotificacaoEntity entity = invocation.getArgument(0);
          entity.setId("not-2");
          return entity;
        });
    when(outboxPayloadMapper.toJson(any())).thenReturn("{\"event\":\"ok\"}");
    when(outboxEventRepository.save(any(OutboxEventEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Notificacao result = adapter.registrar(notificacaoEnviadaSemId());

    assertEquals("not-2", result.id());

    ArgumentCaptor<NotificacaoEntity> notificacaoCaptor = ArgumentCaptor.forClass(NotificacaoEntity.class);
    verify(notificacaoRepository).save(notificacaoCaptor.capture());
    assertEquals("enc-1", notificacaoCaptor.getValue().getEncomendaId());
    assertEquals("morador-1", notificacaoCaptor.getValue().getMoradorId());

    ArgumentCaptor<OutboxEventEntity> outboxCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);
    verify(outboxEventRepository).save(outboxCaptor.capture());
    assertEquals("NOTIFICACAO", outboxCaptor.getValue().getAggregateType());
    assertEquals("not-2", outboxCaptor.getValue().getAggregateId());
    assertEquals("NotificacaoSolicitada", outboxCaptor.getValue().getType());
    assertEquals("{\"event\":\"ok\"}", outboxCaptor.getValue().getPayload());
    assertEquals(Instant.parse("2026-02-25T12:00:00Z"), outboxCaptor.getValue().getEventTimestamp());
  }

  @Test
  void deveTratarConflitoDeConcorrenciaRetornandoDuplicada() {
    NotificacaoEntity duplicada = entityComStatus(StatusNotificacao.ENVIADA);
    duplicada.setId("not-3");
    duplicada.setCreatedAt(Instant.parse("2026-02-25T10:00:00Z"));
    duplicada.setSentAt(Instant.parse("2026-02-25T10:01:00Z"));

    when(notificacaoRepository.findBySourceEventIdAndMoradorId("source-1", "morador-1"))
        .thenReturn(Optional.empty(), Optional.of(duplicada));
    when(notificacaoRepository.save(any(NotificacaoEntity.class)))
        .thenThrow(new DataIntegrityViolationException("duplicado"));

    Notificacao result = adapter.registrar(notificacaoEnviadaSemId());

    assertEquals("not-3", result.id());
    assertEquals(StatusNotificacao.ENVIADA, result.status());
    verify(outboxEventRepository, never()).save(any(OutboxEventEntity.class));
  }

  @Test
  void devePropagarConflitoQuandoDuplicadaNaoForEncontrada() {
    DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicado");

    when(notificacaoRepository.findBySourceEventIdAndMoradorId("source-1", "morador-1"))
        .thenReturn(Optional.empty(), Optional.empty());
    when(notificacaoRepository.save(any(NotificacaoEntity.class))).thenThrow(exception);

    assertThrows(DataIntegrityViolationException.class, () -> adapter.registrar(notificacaoEnviadaSemId()));
    verify(outboxEventRepository, never()).save(any(OutboxEventEntity.class));
  }

  @Test
  void deveFalharQuandoReconstrucaoDaExistenteNaoTiverCreatedAt() {
    NotificacaoEntity existente = entityComStatus(StatusNotificacao.ENVIADA);
    existente.setId("not-4");
    existente.setCreatedAt(null);
    existente.setSentAt(null);

    when(notificacaoRepository.findBySourceEventIdAndMoradorId("source-1", "morador-1"))
        .thenReturn(Optional.of(existente));

    assertThrows(IllegalArgumentException.class, () -> adapter.registrar(notificacaoEnviadaSemId()));
  }

  private Notificacao notificacaoEnviadaSemId() {
    Notificacao notificacao = Notificacao.criar(
        "enc-1",
        "morador-1",
        CanalNotificacao.PUSH,
        "device-1",
        "Sua encomenda chegou",
        "source-1",
        "corr-1",
        Instant.parse("2026-02-25T10:00:00Z")
    );
    notificacao.marcarEnviada(Instant.parse("2026-02-25T10:01:00Z"));
    return notificacao;
  }

  private NotificacaoEntity entityComStatus(StatusNotificacao status) {
    NotificacaoEntity entity = new NotificacaoEntity();
    entity.setEncomendaId("enc-1");
    entity.setMoradorId("morador-1");
    entity.setCanal(CanalNotificacao.PUSH);
    entity.setDestino("device-1");
    entity.setMensagem("Sua encomenda chegou");
    entity.setStatus(status);
    entity.setSourceEventId("source-1");
    entity.setCorrelationId("corr-1");
    return entity;
  }
}

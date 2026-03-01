package br.com.condominio.servico.notificacao.adapter.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.Notificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import br.com.condominio.servico.notificacao.infrastructure.persistence.entity.NotificacaoEntity;
import br.com.condominio.servico.notificacao.infrastructure.persistence.repository.SpringDataNotificacaoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificacaoRepositoryAdapterTest {

  @Mock
  private SpringDataNotificacaoRepository notificacaoRepository;

  private NotificacaoRepositoryAdapter adapter;

  @BeforeEach
  void setup() {
    adapter = new NotificacaoRepositoryAdapter(notificacaoRepository);
  }

  @Test
  void deveFalharAoSalvarQuandoNotificacaoForNula() {
    assertThrows(IllegalArgumentException.class, () -> adapter.salvar(null));
  }

  @Test
  void deveGerarIdQuandoSalvarNotificacaoSemId() {
    Notificacao notificacao = notificacaoEnviadaSemId();
    when(notificacaoRepository.save(any(NotificacaoEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Notificacao salva = adapter.salvar(notificacao);

    assertNotNull(salva.id());
    assertEquals(StatusNotificacao.ENVIADA, salva.status());

    ArgumentCaptor<NotificacaoEntity> captor = ArgumentCaptor.forClass(NotificacaoEntity.class);
    verify(notificacaoRepository).save(captor.capture());
    assertNotNull(captor.getValue().getId());
    assertEquals("enc-1", captor.getValue().getEncomendaId());
    assertEquals("morador-1", captor.getValue().getMoradorId());
  }

  @Test
  void deveBuscarPorIdEMapearStatusFalhaComRazaoPadrao() {
    NotificacaoEntity entity = baseEntity();
    entity.setId("not-1");
    entity.setStatus(StatusNotificacao.FALHA);
    entity.setCreatedAt(Instant.parse("2026-02-25T10:00:00Z"));
    entity.setFailedAt(null);
    entity.setFailureReason(" ");

    when(notificacaoRepository.findById("not-1")).thenReturn(Optional.of(entity));

    Notificacao notificacao = adapter.buscarPorId("not-1").orElseThrow();
    assertEquals(StatusNotificacao.FALHA, notificacao.status());
    assertEquals("Falha de envio", notificacao.motivoFalha());
    assertEquals(Instant.parse("2026-02-25T10:00:00Z"), notificacao.falhaEm());
  }

  @Test
  void deveBuscarPorIdEMapearStatusConfirmada() {
    NotificacaoEntity entity = baseEntity();
    entity.setId("not-2");
    entity.setStatus(StatusNotificacao.CONFIRMADA);
    entity.setCreatedAt(Instant.parse("2026-02-25T10:00:00Z"));
    entity.setSentAt(null);
    entity.setConfirmedAt(null);

    when(notificacaoRepository.findById("not-2")).thenReturn(Optional.of(entity));

    Notificacao notificacao = adapter.buscarPorId("not-2").orElseThrow();
    assertEquals(StatusNotificacao.CONFIRMADA, notificacao.status());
    assertEquals(Instant.parse("2026-02-25T10:00:00Z"), notificacao.confirmadaEm());
  }

  @Test
  void deveFalharAoBuscarPorIdQuandoCreatedAtNaoExistirParaReconstrucao() {
    NotificacaoEntity entity = baseEntity();
    entity.setId("not-3");
    entity.setStatus(StatusNotificacao.ENVIADA);
    entity.setCreatedAt(null);
    entity.setSentAt(null);

    when(notificacaoRepository.findById("not-3")).thenReturn(Optional.of(entity));

    assertThrows(IllegalArgumentException.class, () -> adapter.buscarPorId("not-3"));
  }

  @Test
  void deveValidarParametrosDeBuscaEListagem() {
    assertThrows(IllegalArgumentException.class, () -> adapter.buscarPorId(" "));
    assertThrows(IllegalArgumentException.class, () -> adapter.listarNaoConfirmadasPorMorador(null, 0, 20));
    assertThrows(IllegalArgumentException.class, () -> adapter.listarNaoConfirmadasPorMorador("morador-1", -1, 20));
    assertThrows(IllegalArgumentException.class, () -> adapter.listarNaoConfirmadasPorMorador("morador-1", 0, 0));
  }

  @Test
  void deveListarNaoConfirmadasPorMorador() {
    NotificacaoEntity entity = baseEntity();
    entity.setId("not-4");
    entity.setStatus(StatusNotificacao.ENVIADA);
    entity.setCreatedAt(Instant.parse("2026-02-25T10:00:00Z"));
    entity.setSentAt(Instant.parse("2026-02-25T10:01:00Z"));

    when(notificacaoRepository.findByMoradorIdAndStatusNot(
        eq("morador-1"),
        eq(StatusNotificacao.CONFIRMADA),
        any(Pageable.class)
    )).thenReturn(new PageImpl<>(List.of(entity)));

    List<Notificacao> notificacoes = adapter.listarNaoConfirmadasPorMorador("morador-1", 0, 10);

    assertEquals(1, notificacoes.size());
    assertEquals("not-4", notificacoes.getFirst().id());
    assertEquals(StatusNotificacao.ENVIADA, notificacoes.getFirst().status());
    assertEquals(Instant.parse("2026-02-25T10:01:00Z"), notificacoes.getFirst().enviadaEm());
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

  private NotificacaoEntity baseEntity() {
    NotificacaoEntity entity = new NotificacaoEntity();
    entity.setEncomendaId("enc-1");
    entity.setMoradorId("morador-1");
    entity.setCanal(CanalNotificacao.PUSH);
    entity.setDestino("device-1");
    entity.setMensagem("Sua encomenda chegou");
    entity.setSourceEventId("source-1");
    entity.setCorrelationId("corr-1");
    return entity;
  }
}

package br.com.condominio.servico.notificacao.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificacaoTest {

  @Test
  void deveCriarNotificacaoPendenteEAtribuirId() {
    Notificacao notificacao = baseNotificacao();

    assertEquals(StatusNotificacao.PENDENTE, notificacao.status());
    assertEquals("enc-1", notificacao.encomendaId());
    assertEquals("morador-1", notificacao.moradorId());
    assertEquals("corr-1", notificacao.correlationId());

    notificacao.atribuirId("not-1");
    assertEquals("not-1", notificacao.id());
  }

  @Test
  void devePermitirFluxoFalhaReenvioEConfirmacao() {
    Notificacao notificacao = baseNotificacao();

    notificacao.marcarFalha("timeout", Instant.parse("2026-02-25T10:01:00Z"));
    assertEquals(StatusNotificacao.FALHA, notificacao.status());
    assertEquals("timeout", notificacao.motivoFalha());

    notificacao.marcarEnviada(Instant.parse("2026-02-25T10:02:00Z"));
    assertEquals(StatusNotificacao.ENVIADA, notificacao.status());
    assertEquals("morador-1", notificacao.moradorId());

    notificacao.confirmarRecebimento("morador-1", Instant.parse("2026-02-25T10:03:00Z"));
    assertEquals(StatusNotificacao.CONFIRMADA, notificacao.status());
    assertEquals(Instant.parse("2026-02-25T10:03:00Z"), notificacao.confirmadaEm());
  }

  @Test
  void deveValidarTransicoesInvalidas() {
    Notificacao notificacao = baseNotificacao();

    assertThrows(
        IllegalStateException.class,
        () -> notificacao.confirmarRecebimento("morador-1", Instant.parse("2026-02-25T10:01:00Z"))
    );

    notificacao.marcarEnviada(Instant.parse("2026-02-25T10:02:00Z"));
    assertThrows(
        IllegalArgumentException.class,
        () -> notificacao.confirmarRecebimento("morador-x", Instant.parse("2026-02-25T10:03:00Z"))
    );

    notificacao.confirmarRecebimento("morador-1", Instant.parse("2026-02-25T10:03:00Z"));
    assertThrows(
        IllegalStateException.class,
        () -> notificacao.marcarFalha("erro", Instant.parse("2026-02-25T10:04:00Z"))
    );
    assertThrows(
        IllegalStateException.class,
        () -> notificacao.marcarEnviada(Instant.parse("2026-02-25T10:05:00Z"))
    );
  }

  private Notificacao baseNotificacao() {
    return Notificacao.criar(
        "enc-1",
        "morador-1",
        CanalNotificacao.PUSH,
        "device-1",
        "Sua encomenda chegou",
        "source-1",
        "corr-1",
        Instant.parse("2026-02-25T10:00:00Z")
    );
  }
}

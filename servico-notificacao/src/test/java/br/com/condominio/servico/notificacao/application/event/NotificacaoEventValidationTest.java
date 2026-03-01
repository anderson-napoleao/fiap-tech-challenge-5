package br.com.condominio.servico.notificacao.application.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificacaoEventValidationTest {

  @Test
  void deveCriarNotificacaoSolicitadaComPayloadValido() {
    NotificacaoSolicitadaEvent event = new NotificacaoSolicitadaEvent(
        "evt-1",
        1,
        Instant.parse("2026-02-25T12:00:00Z"),
        "not-1",
        "enc-1",
        "morador-1",
        CanalNotificacao.PUSH,
        "device-1",
        "Sua encomenda chegou",
        "corr-1",
        StatusNotificacao.ENVIADA
    );

    assertEquals("evt-1", event.eventId());
    assertEquals("not-1", event.notificacaoId());
    assertEquals(CanalNotificacao.PUSH, event.canal());
    assertEquals(StatusNotificacao.ENVIADA, event.status());
  }

  @Test
  void deveValidarCamposObrigatoriosDaNotificacaoSolicitada() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new NotificacaoSolicitadaEvent(
            " ",
            1,
            Instant.parse("2026-02-25T12:00:00Z"),
            "not-1",
            "enc-1",
            "morador-1",
            CanalNotificacao.PUSH,
            "device-1",
            "msg",
            "corr-1",
            StatusNotificacao.ENVIADA
        )
    );

    assertThrows(
        IllegalArgumentException.class,
        () -> new NotificacaoSolicitadaEvent(
            "evt-1",
            0,
            Instant.parse("2026-02-25T12:00:00Z"),
            "not-1",
            "enc-1",
            "morador-1",
            CanalNotificacao.PUSH,
            "device-1",
            "msg",
            "corr-1",
            StatusNotificacao.ENVIADA
        )
    );
  }

  @Test
  void deveCriarNotificacaoEnviadaComPayloadValido() {
    NotificacaoEnviadaEvent event = new NotificacaoEnviadaEvent(
        "evt-1",
        1,
        Instant.parse("2026-02-25T12:00:00Z"),
        "not-1",
        "enc-1",
        "morador-1",
        "corr-1"
    );

    assertEquals("evt-1", event.eventId());
    assertEquals("enc-1", event.encomendaId());
  }

  @Test
  void deveValidarCamposObrigatoriosDaNotificacaoEnviada() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new NotificacaoEnviadaEvent(
            "evt-1",
            1,
            null,
            "not-1",
            "enc-1",
            "morador-1",
            "corr-1"
        )
    );
  }

  @Test
  void deveCriarNotificacaoFalhouComPayloadValido() {
    NotificacaoFalhouEvent event = new NotificacaoFalhouEvent(
        "evt-1",
        1,
        Instant.parse("2026-02-25T12:00:00Z"),
        "not-1",
        "enc-1",
        "morador-1",
        "Timeout do provedor",
        "corr-1"
    );

    assertEquals("Timeout do provedor", event.motivoFalha());
    assertEquals("corr-1", event.correlationId());
  }

  @Test
  void deveValidarCamposObrigatoriosDaNotificacaoFalhou() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new NotificacaoFalhouEvent(
            "evt-1",
            1,
            Instant.parse("2026-02-25T12:00:00Z"),
            "not-1",
            "enc-1",
            "morador-1",
            " ",
            "corr-1"
        )
    );
  }

  @Test
  void deveCriarNotificacaoConfirmadaComPayloadValido() {
    NotificacaoConfirmadaEvent event = new NotificacaoConfirmadaEvent(
        "evt-1",
        1,
        Instant.parse("2026-02-25T12:00:00Z"),
        "not-1",
        "enc-1",
        "morador-1",
        "corr-1"
    );

    assertEquals("not-1", event.notificacaoId());
    assertEquals("morador-1", event.moradorId());
  }

  @Test
  void deveValidarCamposObrigatoriosDaNotificacaoConfirmada() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new NotificacaoConfirmadaEvent(
            "evt-1",
            1,
            Instant.parse("2026-02-25T12:00:00Z"),
            "",
            "enc-1",
            "morador-1",
            "corr-1"
        )
    );
  }
}

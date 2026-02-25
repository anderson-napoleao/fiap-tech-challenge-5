package br.com.condominio.servico.notificacao.application.port.in;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import org.junit.jupiter.api.Test;

class UseCaseCommandValidationTest {

  @Test
  void processarEncomendaRecebidaCommandDeveValidarCamposObrigatorios() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ProcessarEncomendaRecebidaUseCase.Command(
            null,
            "morador-1",
            CanalNotificacao.PUSH,
            "device-1",
            "msg",
            "source-1",
            "corr-1"
        )
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ProcessarEncomendaRecebidaUseCase.Command(
            "enc-1",
            " ",
            CanalNotificacao.PUSH,
            "device-1",
            "msg",
            "source-1",
            "corr-1"
        )
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ProcessarEncomendaRecebidaUseCase.Command(
            "enc-1",
            "morador-1",
            null,
            "device-1",
            "msg",
            "source-1",
            "corr-1"
        )
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ProcessarEncomendaRecebidaUseCase.Command(
            "enc-1",
            "morador-1",
            CanalNotificacao.PUSH,
            "",
            "msg",
            "source-1",
            "corr-1"
        )
    );
  }

  @Test
  void processarEncomendaRecebidaCommandDeveAceitarPayloadValido() {
    assertDoesNotThrow(
        () -> new ProcessarEncomendaRecebidaUseCase.Command(
            "enc-1",
            "morador-1",
            CanalNotificacao.PUSH,
            "device-1",
            "Sua encomenda chegou",
            "source-1",
            "corr-1"
        )
    );
  }

  @Test
  void confirmarRecebimentoCommandDeveValidarCamposObrigatorios() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ConfirmarRecebimentoNotificacaoUseCase.Command(null, "morador-1")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ConfirmarRecebimentoNotificacaoUseCase.Command("not-1", "")
    );
  }

  @Test
  void confirmarRecebimentoCommandDeveAceitarPayloadValido() {
    assertDoesNotThrow(
        () -> new ConfirmarRecebimentoNotificacaoUseCase.Command("not-1", "morador-1")
    );
  }

  @Test
  void listarPendentesCommandDeveValidarCamposObrigatorios() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ListarNotificacoesPendentesUseCase.Command(null, false, 0, 20)
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ListarNotificacoesPendentesUseCase.Command("morador-1", true, 0, 20)
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ListarNotificacoesPendentesUseCase.Command("morador-1", false, -1, 20)
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ListarNotificacoesPendentesUseCase.Command("morador-1", false, 0, 0)
    );
  }

  @Test
  void listarPendentesCommandDeveAceitarPayloadValido() {
    assertDoesNotThrow(
        () -> new ListarNotificacoesPendentesUseCase.Command("morador-1", false, 0, 20)
    );
  }
}

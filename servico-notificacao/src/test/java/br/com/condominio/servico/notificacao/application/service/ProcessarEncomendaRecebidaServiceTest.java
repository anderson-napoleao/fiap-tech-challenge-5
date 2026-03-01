package br.com.condominio.servico.notificacao.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaUseCase;
import br.com.condominio.servico.notificacao.application.port.out.RegistrarNotificacaoComOutboxPort;
import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.Notificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessarEncomendaRecebidaServiceTest {

  @Mock
  private RegistrarNotificacaoComOutboxPort registrarNotificacaoComOutboxPort;

  private final Clock fixedClock = Clock.fixed(Instant.parse("2026-02-25T12:00:00Z"), ZoneOffset.UTC);
  private ProcessarEncomendaRecebidaService service;

  @BeforeEach
  void setup() {
    service = new ProcessarEncomendaRecebidaService(registrarNotificacaoComOutboxPort, fixedClock);
  }

  @Test
  void deveValidarDependenciasNoConstrutor() {
    assertThrows(
        NullPointerException.class,
        () -> new ProcessarEncomendaRecebidaService(null, fixedClock)
    );
    assertThrows(
        NullPointerException.class,
        () -> new ProcessarEncomendaRecebidaService(registrarNotificacaoComOutboxPort, null)
    );
  }

  @Test
  void deveRejeitarComandoNulo() {
    assertThrows(IllegalArgumentException.class, () -> service.executar(null));
  }

  @Test
  void deveCriarNotificacaoComStatusEnviadaERegistrarNaOutbox() {
    when(registrarNotificacaoComOutboxPort.registrar(any(Notificacao.class)))
        .thenAnswer(invocation -> {
          Notificacao notificacao = invocation.getArgument(0);
          return notificacao.atribuirId("not-1");
        });

    ProcessarEncomendaRecebidaUseCase.Result result = service.executar(
        new ProcessarEncomendaRecebidaUseCase.Command(
            "enc-1",
            "morador-1",
            CanalNotificacao.PUSH,
            "device-1",
            "Sua encomenda chegou",
            "source-1",
            "corr-1"
        )
    );

    assertEquals("not-1", result.id());
    assertEquals("enc-1", result.encomendaId());
    assertEquals("morador-1", result.moradorId());
    assertEquals(CanalNotificacao.PUSH, result.canal());
    assertEquals("device-1", result.destino());
    assertEquals("Sua encomenda chegou", result.mensagem());
    assertEquals(StatusNotificacao.ENVIADA, result.status());
    assertEquals("source-1", result.sourceEventId());
    assertEquals("corr-1", result.correlationId());
    assertEquals(Instant.parse("2026-02-25T12:00:00Z"), result.criadaEm());

    ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
    verify(registrarNotificacaoComOutboxPort).registrar(captor.capture());
    Notificacao registrada = captor.getValue();
    assertEquals(StatusNotificacao.ENVIADA, registrada.status());
    assertEquals(Instant.parse("2026-02-25T12:00:00Z"), registrada.criadaEm());
    assertEquals(Instant.parse("2026-02-25T12:00:00Z"), registrada.enviadaEm());
  }
}

package br.com.condominio.servico.notificacao.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.notificacao.application.exception.AcessoNegadoException;
import br.com.condominio.servico.notificacao.application.exception.NotificacaoNaoEncontradaException;
import br.com.condominio.servico.notificacao.application.port.in.ConfirmarRecebimentoNotificacaoUseCase;
import br.com.condominio.servico.notificacao.application.port.out.NotificacaoRepositoryPort;
import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.Notificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ConfirmarRecebimentoNotificacaoServiceTest {

  @Mock
  private NotificacaoRepositoryPort notificacaoRepositoryPort;

  private ConfirmarRecebimentoNotificacaoService service;

  private final Clock fixedClock = Clock.fixed(Instant.parse("2026-02-25T12:00:00Z"), ZoneOffset.UTC);

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    service = new ConfirmarRecebimentoNotificacaoService(notificacaoRepositoryPort, fixedClock);
  }

  @Test
  void deveRetornarNotFoundQuandoNotificacaoNaoExiste() {
    when(notificacaoRepositoryPort.buscarPorId("not-1")).thenReturn(Optional.empty());

    assertThrows(
        NotificacaoNaoEncontradaException.class,
        () -> service.executar(new ConfirmarRecebimentoNotificacaoUseCase.Command("not-1", "morador-1"))
    );
  }

  @Test
  void deveRetornarAcessoNegadoQuandoMoradorNaoPertenceAoContextoDaEncomenda() {
    Notificacao notificacao = criarNotificacaoEnviada("not-1", "enc-1", "morador-2");
    when(notificacaoRepositoryPort.buscarPorId("not-1")).thenReturn(Optional.of(notificacao));
    when(notificacaoRepositoryPort.existePorEncomendaEMorador("enc-1", "morador-1")).thenReturn(false);

    assertThrows(
        AcessoNegadoException.class,
        () -> service.executar(new ConfirmarRecebimentoNotificacaoUseCase.Command("not-1", "morador-1"))
    );
  }

  @Test
  void devePermitirConfirmacaoPorOutroMoradorDaMesmaEncomenda() {
    Notificacao notificacao = criarNotificacaoEnviada("not-1", "enc-1", "morador-2");
    when(notificacaoRepositoryPort.buscarPorId("not-1")).thenReturn(Optional.of(notificacao));
    when(notificacaoRepositoryPort.existePorEncomendaEMorador("enc-1", "morador-1")).thenReturn(true);
    when(notificacaoRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ConfirmarRecebimentoNotificacaoUseCase.Result result = service.executar(
        new ConfirmarRecebimentoNotificacaoUseCase.Command("not-1", "morador-1")
    );

    assertEquals("not-1", result.id());
    assertEquals(StatusNotificacao.CONFIRMADA, result.status());
    assertEquals(Instant.parse("2026-02-25T12:00:00Z"), result.confirmadaEm());
    verify(notificacaoRepositoryPort).salvar(any(Notificacao.class));
  }

  private Notificacao criarNotificacaoEnviada(String id, String encomendaId, String moradorId) {
    Notificacao notificacao = Notificacao.criar(
        encomendaId,
        moradorId,
        CanalNotificacao.PUSH,
        "destino",
        "mensagem",
        "source-1",
        "corr-1",
        Instant.parse("2026-02-25T11:00:00Z")
    ).atribuirId(id);
    notificacao.marcarEnviada(Instant.parse("2026-02-25T11:01:00Z"));
    return notificacao;
  }
}

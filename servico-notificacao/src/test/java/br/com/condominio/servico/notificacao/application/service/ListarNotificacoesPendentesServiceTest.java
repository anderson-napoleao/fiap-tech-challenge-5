package br.com.condominio.servico.notificacao.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.notificacao.application.port.in.ListarNotificacoesPendentesUseCase;
import br.com.condominio.servico.notificacao.application.port.out.NotificacaoRepositoryPort;
import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.Notificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListarNotificacoesPendentesServiceTest {

  @Mock
  private NotificacaoRepositoryPort notificacaoRepositoryPort;

  private ListarNotificacoesPendentesService service;

  @BeforeEach
  void setup() {
    service = new ListarNotificacoesPendentesService(notificacaoRepositoryPort);
  }

  @Test
  void deveValidarDependenciaNoConstrutor() {
    assertThrows(NullPointerException.class, () -> new ListarNotificacoesPendentesService(null));
  }

  @Test
  void deveRejeitarComandoNulo() {
    assertThrows(IllegalArgumentException.class, () -> service.executar(null));
  }

  @Test
  void deveMapearNotificacoesPendentes() {
    Notificacao notificacao = Notificacao.criar(
        "enc-1",
        "morador-1",
        CanalNotificacao.PUSH,
        "device-1",
        "Sua encomenda chegou",
        "source-1",
        "corr-1",
        Instant.parse("2026-02-25T11:00:00Z")
    ).atribuirId("not-1");
    notificacao.marcarEnviada(Instant.parse("2026-02-25T11:01:00Z"));

    when(notificacaoRepositoryPort.listarNaoConfirmadasPorMorador("morador-1", 0, 20))
        .thenReturn(List.of(notificacao));

    ListarNotificacoesPendentesUseCase.Result result = service.executar(
        new ListarNotificacoesPendentesUseCase.Command("morador-1", false, 0, 20)
    );

    assertEquals(1, result.notificacoes().size());
    assertEquals(0, result.page());
    assertEquals(20, result.size());
    assertEquals("not-1", result.notificacoes().getFirst().id());
    assertEquals(StatusNotificacao.ENVIADA, result.notificacoes().getFirst().status());
    assertEquals(Instant.parse("2026-02-25T11:01:00Z"), result.notificacoes().getFirst().enviadaEm());
  }
}

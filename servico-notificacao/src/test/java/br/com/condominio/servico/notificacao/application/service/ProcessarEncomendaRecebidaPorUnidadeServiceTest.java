package br.com.condominio.servico.notificacao.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaPorUnidadeUseCase;
import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaUseCase;
import br.com.condominio.servico.notificacao.application.port.out.MoradorDirectoryPort;
import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ProcessarEncomendaRecebidaPorUnidadeServiceTest {

  @Mock
  private MoradorDirectoryPort moradorDirectoryPort;

  @Mock
  private ProcessarEncomendaRecebidaUseCase processarEncomendaRecebidaUseCase;

  private ProcessarEncomendaRecebidaPorUnidadeService service;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    service = new ProcessarEncomendaRecebidaPorUnidadeService(
        moradorDirectoryPort,
        processarEncomendaRecebidaUseCase
    );
  }

  @Test
  void deveProcessarNotificacaoParaTodosMoradoresDaUnidade() {
    when(moradorDirectoryPort.listarMoradoresPorUnidade("A", "101"))
        .thenReturn(List.of(
            new MoradorDirectoryPort.Morador("id-1", "Maria", "maria@local"),
            new MoradorDirectoryPort.Morador("id-2", "Joao", "joao@local")
        ));
    when(processarEncomendaRecebidaUseCase.executar(any()))
        .thenReturn(new ProcessarEncomendaRecebidaUseCase.Result(
            "not-1",
            "enc-1",
            "id-1",
            CanalNotificacao.PUSH,
            "id-1",
            "mensagem",
            StatusNotificacao.ENVIADA,
            "source-1",
            "corr-1",
            Instant.parse("2026-02-25T00:00:00Z")
        ));

    ProcessarEncomendaRecebidaPorUnidadeUseCase.Result result = service.executar(
        new ProcessarEncomendaRecebidaPorUnidadeUseCase.Command(
            "event-1",
            1L,
            "Maria",
            "101",
            "A",
            "Caixa"
        )
    );

    assertEquals(1L, result.encomendaId());
    assertEquals(2, result.moradoresEncontrados());
    assertEquals(2, result.notificacoesProcessadas());
    verify(processarEncomendaRecebidaUseCase, times(2)).executar(any());
  }
}

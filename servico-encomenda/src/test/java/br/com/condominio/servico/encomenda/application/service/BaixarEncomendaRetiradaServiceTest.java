package br.com.condominio.servico.encomenda.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.encomenda.application.exception.EncomendaNaoEncontradaException;
import br.com.condominio.servico.encomenda.application.port.in.BaixarEncomendaRetiradaUseCase;
import br.com.condominio.servico.encomenda.application.port.out.EncomendaRepositoryPort;
import br.com.condominio.servico.encomenda.domain.Encomenda;
import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaixarEncomendaRetiradaServiceTest {

  @Mock
  private EncomendaRepositoryPort encomendaRepositoryPort;

  private BaixarEncomendaRetiradaService service;

  @BeforeEach
  void setup() {
    Clock clock = Clock.fixed(Instant.parse("2026-02-25T10:10:00Z"), ZoneOffset.UTC);
    service = new BaixarEncomendaRetiradaService(encomendaRepositoryPort, clock);
  }

  @Test
  void deveFalharQuandoComandoForNulo() {
    assertThrows(IllegalArgumentException.class, () -> service.executar(null));
  }

  @Test
  void deveRegistrarRetiradaComSucesso() {
    Encomenda encomenda = Encomenda.receber(
            "Maria",
            "101",
            "A",
            "Caixa",
            "porteiro-1",
            Instant.parse("2026-02-25T10:00:00Z")
        )
        .atribuirId(1L);
    when(encomendaRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(encomenda));
    when(encomendaRepositoryPort.salvar(any(Encomenda.class))).thenAnswer(invocation -> invocation.getArgument(0));

    BaixarEncomendaRetiradaUseCase.Result result = service.executar(
        new BaixarEncomendaRetiradaUseCase.Command(1L, "Maria")
    );

    assertEquals(1L, result.encomendaId());
    assertEquals(StatusEncomenda.RETIRADA, result.status());
    assertEquals("Maria", result.retiradoPorNome());
  }

  @Test
  void deveLancarExcecaoQuandoEncomendaNaoExiste() {
    when(encomendaRepositoryPort.buscarPorId(1L)).thenReturn(Optional.empty());

    assertThrows(
        EncomendaNaoEncontradaException.class,
        () -> service.executar(new BaixarEncomendaRetiradaUseCase.Command(1L, "Maria"))
    );
  }
}


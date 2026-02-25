package br.com.condominio.servico.encomenda.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.encomenda.application.exception.EncomendaNaoEncontradaException;
import br.com.condominio.servico.encomenda.application.port.in.BuscarEncomendaPorIdUseCase;
import br.com.condominio.servico.encomenda.application.port.out.EncomendaRepositoryPort;
import br.com.condominio.servico.encomenda.domain.Encomenda;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BuscarEncomendaPorIdServiceTest {

  @Mock
  private EncomendaRepositoryPort encomendaRepositoryPort;

  private BuscarEncomendaPorIdService service;

  @BeforeEach
  void setup() {
    service = new BuscarEncomendaPorIdService(encomendaRepositoryPort);
  }

  @Test
  void deveFalharQuandoComandoForNulo() {
    assertThrows(IllegalArgumentException.class, () -> service.executar(null));
  }

  @Test
  void deveRetornarEncomendaQuandoEncontrada() {
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

    BuscarEncomendaPorIdUseCase.Result result = service.executar(
        new BuscarEncomendaPorIdUseCase.Command(1L)
    );

    assertEquals(1L, result.id());
    assertEquals("Maria", result.nomeDestinatario());
  }

  @Test
  void deveLancarExcecaoQuandoNaoEncontrada() {
    when(encomendaRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

    assertThrows(
        EncomendaNaoEncontradaException.class,
        () -> service.executar(new BuscarEncomendaPorIdUseCase.Command(99L))
    );
  }
}


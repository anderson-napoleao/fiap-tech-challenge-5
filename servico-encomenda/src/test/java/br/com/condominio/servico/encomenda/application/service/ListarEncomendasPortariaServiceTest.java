package br.com.condominio.servico.encomenda.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.encomenda.application.port.in.ListarEncomendasPortariaUseCase;
import br.com.condominio.servico.encomenda.application.port.out.EncomendaRepositoryPort;
import br.com.condominio.servico.encomenda.domain.Encomenda;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListarEncomendasPortariaServiceTest {

  @Mock
  private EncomendaRepositoryPort encomendaRepositoryPort;

  private ListarEncomendasPortariaService service;

  @BeforeEach
  void setup() {
    service = new ListarEncomendasPortariaService(encomendaRepositoryPort);
  }

  @Test
  void deveFalharQuandoComandoForNulo() {
    assertThrows(IllegalArgumentException.class, () -> service.executar(null));
  }

  @Test
  void deveListarEncomendasComPaginacao() {
    Encomenda encomenda = Encomenda.receber(
            "Maria",
            "101",
            "A",
            "Caixa",
            "porteiro-1",
            Instant.parse("2026-02-25T10:00:00Z")
        )
        .atribuirId(1L);

    when(encomendaRepositoryPort.listar(
        new EncomendaRepositoryPort.FiltroListagem("101", "A", LocalDate.parse("2026-02-25"), 0, 10)
    )).thenReturn(new EncomendaRepositoryPort.ResultadoListagem(List.of(encomenda), 1, 1));

    ListarEncomendasPortariaUseCase.Result result = service.executar(
        new ListarEncomendasPortariaUseCase.Command("101", "A", LocalDate.parse("2026-02-25"), 0, 10)
    );

    assertEquals(1, result.encomendas().size());
    assertEquals(1L, result.encomendas().getFirst().id());
    assertEquals(1, result.totalPages());
  }
}


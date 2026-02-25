package br.com.condominio.servico.encomenda.application.port.in;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class UseCaseCommandValidationTest {

  @Test
  void receberEncomendaCommandDeveValidarCamposObrigatorios() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ReceberEncomendaUseCase.Command(null, "101", "A", "Caixa", "porteiro-1")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ReceberEncomendaUseCase.Command("Maria", " ", "A", "Caixa", "porteiro-1")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ReceberEncomendaUseCase.Command("Maria", "101", "", "Caixa", "porteiro-1")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ReceberEncomendaUseCase.Command("Maria", "101", "A", " ", "porteiro-1")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ReceberEncomendaUseCase.Command("Maria", "101", "A", "Caixa", "")
    );
  }

  @Test
  void receberEncomendaCommandDeveAceitarPayloadValido() {
    assertDoesNotThrow(
        () -> new ReceberEncomendaUseCase.Command(
            "Maria",
            "101",
            "A",
            "Caixa pequena",
            "porteiro-1"
        )
    );
  }

  @Test
  void baixarEncomendaRetiradaCommandDeveValidarCamposObrigatorios() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new BaixarEncomendaRetiradaUseCase.Command(null, "Maria")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new BaixarEncomendaRetiradaUseCase.Command(0L, "Maria")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new BaixarEncomendaRetiradaUseCase.Command(1L, " ")
    );
  }

  @Test
  void baixarEncomendaRetiradaCommandDeveAceitarPayloadValido() {
    assertDoesNotThrow(
        () -> new BaixarEncomendaRetiradaUseCase.Command(1L, "Maria")
    );
  }

  @Test
  void listarEncomendasPortariaCommandDeveValidarPaginacao() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ListarEncomendasPortariaUseCase.Command(null, null, null, -1, 10)
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ListarEncomendasPortariaUseCase.Command(null, null, null, 0, 0)
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new ListarEncomendasPortariaUseCase.Command(null, null, null, 0, 101)
    );
  }

  @Test
  void listarEncomendasPortariaCommandDeveAceitarPayloadValido() {
    assertDoesNotThrow(
        () -> new ListarEncomendasPortariaUseCase.Command(" 101 ", " a ", null, 0, 10)
    );
    assertDoesNotThrow(
        () -> new ListarEncomendasPortariaUseCase.Command(
            null,
            null,
            LocalDate.parse("2026-02-25"),
            0,
            10
        )
    );
  }
}

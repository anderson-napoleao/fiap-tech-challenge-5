package br.com.condominio.servico.encomenda.application.port.in;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}

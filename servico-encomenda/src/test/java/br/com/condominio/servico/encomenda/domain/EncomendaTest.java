package br.com.condominio.servico.encomenda.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class EncomendaTest {

  @Test
  void deveMarcarRetiradaQuandoStatusAtualForRecebida() {
    Encomenda encomenda = Encomenda.receber(
        "Maria",
        "101",
        "A",
        "Caixa pequena",
        "porteiro-1",
        Instant.parse("2026-02-21T18:00:00Z")
    );

    encomenda.marcarRetirada(Instant.parse("2026-02-21T18:10:00Z"));

    assertEquals(StatusEncomenda.RETIRADA, encomenda.status());
    assertEquals(Instant.parse("2026-02-21T18:10:00Z"), encomenda.dataRetirada());
  }

  @Test
  void naoDevePermitirDataRetiradaAnteriorAoRecebimento() {
    Encomenda encomenda = Encomenda.receber(
        "Maria",
        "101",
        "A",
        "Caixa pequena",
        "porteiro-1",
        Instant.parse("2026-02-21T18:00:00Z")
    );

    assertThrows(
        IllegalArgumentException.class,
        () -> encomenda.marcarRetirada(Instant.parse("2026-02-21T17:59:59Z"))
    );
  }

  @Test
  void naoDevePermitirNovaRetiradaQuandoJaRetirada() {
    Encomenda encomenda = Encomenda.receber(
        "Maria",
        "101",
        "A",
        "Caixa pequena",
        "porteiro-1",
        Instant.parse("2026-02-21T18:00:00Z")
    );
    encomenda.marcarRetirada(Instant.parse("2026-02-21T18:10:00Z"));

    assertThrows(
        IllegalStateException.class,
        () -> encomenda.marcarRetirada(Instant.parse("2026-02-21T18:20:00Z"))
    );
  }
}

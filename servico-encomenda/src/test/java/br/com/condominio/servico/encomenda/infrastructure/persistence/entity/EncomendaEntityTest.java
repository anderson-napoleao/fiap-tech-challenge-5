package br.com.condominio.servico.encomenda.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class EncomendaEntityTest {

  @Test
  void deveSetarEObterTodosOsCampos() {
    EncomendaEntity entity = new EncomendaEntity();
    Instant recebimento = Instant.parse("2026-02-25T10:00:00Z");
    Instant retirada = Instant.parse("2026-02-25T11:00:00Z");

    entity.setId(1L);
    entity.setNomeDestinatario("Maria");
    entity.setApartamento("101");
    entity.setBloco("A");
    entity.setDescricao("Caixa");
    entity.setRecebidoPor("porteiro-1");
    entity.setStatus(StatusEncomenda.RETIRADA);
    entity.setDataRecebimento(recebimento);
    entity.setDataRetirada(retirada);
    entity.setRetiradoPorNome("Maria");

    assertEquals(1L, entity.getId());
    assertEquals("Maria", entity.getNomeDestinatario());
    assertEquals("101", entity.getApartamento());
    assertEquals("A", entity.getBloco());
    assertEquals("Caixa", entity.getDescricao());
    assertEquals("porteiro-1", entity.getRecebidoPor());
    assertEquals(StatusEncomenda.RETIRADA, entity.getStatus());
    assertEquals(recebimento, entity.getDataRecebimento());
    assertEquals(retirada, entity.getDataRetirada());
    assertEquals("Maria", entity.getRetiradoPorNome());
  }
}


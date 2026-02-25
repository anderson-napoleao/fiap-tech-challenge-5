package br.com.condominio.servico.encomenda.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.condominio.servico.encomenda.application.exception.EncomendaNaoEncontradaException;
import br.com.condominio.servico.encomenda.application.port.in.BaixarEncomendaRetiradaUseCase;
import br.com.condominio.servico.encomenda.application.port.in.ReceberEncomendaUseCase;
import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.EncomendaEntity;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataEncomendaRepository;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration")
@EnabledIfSystemProperty(named = "testcontainers.enabled", matches = "true")
class BaixarEncomendaRetiradaIntegrationTest {

  @Autowired
  private ReceberEncomendaUseCase receberEncomendaUseCase;

  @Autowired
  private BaixarEncomendaRetiradaUseCase baixarEncomendaRetiradaUseCase;

  @Autowired
  private SpringDataEncomendaRepository encomendaRepository;

  @Autowired
  private SpringDataOutboxEventRepository outboxEventRepository;

  @BeforeEach
  void limparBase() {
    outboxEventRepository.deleteAll();
    encomendaRepository.deleteAll();
  }

  @Test
  void deveBaixarEncomendaRetiradaEPersistirNomeDeQuemRetirou() {
    ReceberEncomendaUseCase.Result recebida = receberEncomendaUseCase.executar(
        new ReceberEncomendaUseCase.Command(
            "Maria",
            "101",
            "A",
            "Caixa pequena",
            "porteiro-1"
        )
    );

    BaixarEncomendaRetiradaUseCase.Result retirada = baixarEncomendaRetiradaUseCase.executar(
        new BaixarEncomendaRetiradaUseCase.Command(recebida.id(), "Maria")
    );

    assertEquals(recebida.id(), retirada.encomendaId());
    assertEquals(StatusEncomenda.RETIRADA, retirada.status());
    assertNotNull(retirada.dataRetirada());
    assertEquals("Maria", retirada.retiradoPorNome());

    EncomendaEntity entity = encomendaRepository.findById(recebida.id()).orElseThrow();
    assertEquals(StatusEncomenda.RETIRADA, entity.getStatus());
    assertNotNull(entity.getDataRetirada());
    assertEquals("Maria", entity.getRetiradoPorNome());
  }

  @Test
  void deveLancarErroQuandoEncomendaNaoExistir() {
    assertThrows(
        EncomendaNaoEncontradaException.class,
        () -> baixarEncomendaRetiradaUseCase.executar(new BaixarEncomendaRetiradaUseCase.Command(999L, "Maria"))
    );
  }
}

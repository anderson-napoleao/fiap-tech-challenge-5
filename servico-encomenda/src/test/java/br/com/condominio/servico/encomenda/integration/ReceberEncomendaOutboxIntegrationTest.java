package br.com.condominio.servico.encomenda.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import br.com.condominio.servico.encomenda.adapter.out.OutboxPayloadMapper;
import br.com.condominio.servico.encomenda.application.port.in.ReceberEncomendaUseCase;
import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.OutboxEventEntity;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataEncomendaRepository;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration")
@EnabledIfSystemProperty(named = "testcontainers.enabled", matches = "true")
class ReceberEncomendaOutboxIntegrationTest {

  @Autowired
  private ReceberEncomendaUseCase receberEncomendaUseCase;

  @Autowired
  private SpringDataEncomendaRepository encomendaRepository;

  @Autowired
  private SpringDataOutboxEventRepository outboxEventRepository;

  @SpyBean
  private OutboxPayloadMapper outboxPayloadMapper;

  @BeforeEach
  void limparBase() {
    outboxEventRepository.deleteAll();
    encomendaRepository.deleteAll();
  }

  @AfterEach
  void limparSpies() {
    reset(outboxPayloadMapper);
  }

  @Test
  void devePersistirEncomendaEOutboxNaMesmaTransacao() {
    ReceberEncomendaUseCase.Result result = receberEncomendaUseCase.executar(
        new ReceberEncomendaUseCase.Command(
            "Maria",
            "101",
            "A",
            "Caixa pequena",
            "porteiro-1"
        )
    );

    assertNotNull(result.id());
    assertEquals(1, encomendaRepository.count());
    assertEquals(1, outboxEventRepository.count());

    List<OutboxEventEntity> events = outboxEventRepository.findByAggregateId(String.valueOf(result.id()));
    assertEquals(1, events.size());
    assertTrue(events.getFirst().getPayload().contains("\"bloco\":\"A\""));
    assertTrue(events.getFirst().getPayload().contains("\"status\":\"RECEBIDA\""));
  }

  @Test
  void deveFazerRollbackQuandoFalharAoGerarPayloadOutbox() {
    doThrow(new IllegalStateException("falha simulada"))
        .when(outboxPayloadMapper).toJson(any());

    assertThrows(
        IllegalStateException.class,
        () -> receberEncomendaUseCase.executar(
            new ReceberEncomendaUseCase.Command(
                "Maria",
                "101",
                "A",
                "Caixa pequena",
                "porteiro-1"
            )
        )
    );

    assertEquals(0, encomendaRepository.count());
    assertEquals(0, outboxEventRepository.count());
  }
}

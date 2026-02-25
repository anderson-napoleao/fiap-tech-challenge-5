package br.com.condominio.servico.notificacao.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import br.com.condominio.servico.notificacao.adapter.out.OutboxPayloadMapper;
import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaUseCase;
import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.infrastructure.persistence.entity.OutboxEventEntity;
import br.com.condominio.servico.notificacao.infrastructure.persistence.repository.SpringDataNotificacaoRepository;
import br.com.condominio.servico.notificacao.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import java.util.List;
import java.util.UUID;
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
class ProcessarNotificacaoOutboxIntegrationTest {

  @Autowired
  private ProcessarEncomendaRecebidaUseCase processarEncomendaRecebidaUseCase;

  @Autowired
  private SpringDataNotificacaoRepository notificacaoRepository;

  @Autowired
  private SpringDataOutboxEventRepository outboxEventRepository;

  @SpyBean
  private OutboxPayloadMapper outboxPayloadMapper;

  @BeforeEach
  void limparBase() {
    outboxEventRepository.deleteAll();
    notificacaoRepository.deleteAll();
  }

  @AfterEach
  void limparSpies() {
    reset(outboxPayloadMapper);
  }

  @Test
  void devePersistirNotificacaoEOutboxNaMesmaTransacao() {
    ProcessarEncomendaRecebidaUseCase.Result result = processarEncomendaRecebidaUseCase.executar(
        new ProcessarEncomendaRecebidaUseCase.Command(
            "enc-100",
            "morador-200",
            CanalNotificacao.PUSH,
            "device-token-abc",
            "Sua encomenda chegou na portaria",
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        )
    );

    assertNotNull(result.id());
    assertEquals(1, notificacaoRepository.count());
    assertEquals(1, outboxEventRepository.count());

    List<OutboxEventEntity> events = outboxEventRepository.findByAggregateId(result.id());
    assertEquals(1, events.size());
    assertTrue(events.getFirst().getPayload().contains("\"status\":\"ENVIADA\""));
    assertTrue(events.getFirst().getPayload().contains("\"encomendaId\":\"enc-100\""));
  }

  @Test
  void deveFazerRollbackQuandoFalharAoGerarPayloadOutbox() {
    doThrow(new IllegalStateException("falha simulada"))
        .when(outboxPayloadMapper).toJson(any());

    assertThrows(
        IllegalStateException.class,
        () -> processarEncomendaRecebidaUseCase.executar(
            new ProcessarEncomendaRecebidaUseCase.Command(
                "enc-101",
                "morador-201",
                CanalNotificacao.EMAIL,
                "morador@condominio.local",
                "Sua encomenda chegou na portaria",
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
            )
        )
    );

    assertEquals(0, notificacaoRepository.count());
    assertEquals(0, outboxEventRepository.count());
  }

  @Test
  void reprocessamentoDoMesmoSourceEventIdNaoDeveDuplicarNotificacao() {
    String sourceEventId = UUID.randomUUID().toString();

    ProcessarEncomendaRecebidaUseCase.Command command = new ProcessarEncomendaRecebidaUseCase.Command(
        "enc-102",
        "morador-202",
        CanalNotificacao.PUSH,
        "device-token-dup",
        "Sua encomenda chegou na portaria",
        sourceEventId,
        UUID.randomUUID().toString()
    );

    ProcessarEncomendaRecebidaUseCase.Result first = processarEncomendaRecebidaUseCase.executar(command);
    ProcessarEncomendaRecebidaUseCase.Result second = processarEncomendaRecebidaUseCase.executar(command);

    assertEquals(first.id(), second.id());
    assertEquals(1, notificacaoRepository.count());
    assertEquals(1, outboxEventRepository.count());
  }

  @Test
  void mesmoSourceEventIdParaMoradoresDiferentesDevePermitirUmaNotificacaoPorMorador() {
    String sourceEventId = UUID.randomUUID().toString();

    processarEncomendaRecebidaUseCase.executar(new ProcessarEncomendaRecebidaUseCase.Command(
        "enc-103",
        "morador-1",
        CanalNotificacao.PUSH,
        "device-token-1",
        "Sua encomenda chegou na portaria",
        sourceEventId,
        UUID.randomUUID().toString()
    ));

    processarEncomendaRecebidaUseCase.executar(new ProcessarEncomendaRecebidaUseCase.Command(
        "enc-103",
        "morador-2",
        CanalNotificacao.PUSH,
        "device-token-2",
        "Sua encomenda chegou na portaria",
        sourceEventId,
        UUID.randomUUID().toString()
    ));

    assertEquals(2, notificacaoRepository.count());
    assertEquals(2, outboxEventRepository.count());
  }
}

package br.com.condominio.servico.notificacao.adapter.in.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import br.com.condominio.servico.notificacao.application.port.in.ProcessarEncomendaRecebidaPorUnidadeUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EncomendaRecebidaKafkaListenerTest {

  @Mock
  private ProcessarEncomendaRecebidaPorUnidadeUseCase processarEncomendaRecebidaPorUnidadeUseCase;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private EncomendaRecebidaKafkaListener listener;

  @BeforeEach
  void setup() {
    listener = new EncomendaRecebidaKafkaListener(
        objectMapper,
        processarEncomendaRecebidaPorUnidadeUseCase
    );

    lenient().when(processarEncomendaRecebidaPorUnidadeUseCase.executar(any()))
        .thenReturn(new ProcessarEncomendaRecebidaPorUnidadeUseCase.Result(1L, 1, 1));
  }

  @Test
  void deveConsumirPayloadDireto() {
    String payload = """
        {"eventId":"evt-1","eventVersion":1,"occurredAt":"2026-02-25T12:00:00Z","encomendaId":10,
        "nomeDestinatario":"Maria","apartamento":"101","bloco":"A","descricao":"Caixa","recebidoPor":"porteiro","status":"RECEBIDA"}
        """;

    listener.consumir(payload);

    ArgumentCaptor<ProcessarEncomendaRecebidaPorUnidadeUseCase.Command> captor =
        ArgumentCaptor.forClass(ProcessarEncomendaRecebidaPorUnidadeUseCase.Command.class);
    verify(processarEncomendaRecebidaPorUnidadeUseCase).executar(captor.capture());
    assertEquals("evt-1", captor.getValue().eventId());
    assertEquals(10L, captor.getValue().encomendaId());
    assertEquals("101", captor.getValue().apartamento());
  }

  @Test
  void deveConsumirEnvelopeComPayloadTextual() throws Exception {
    String innerPayload = objectMapper.writeValueAsString(Map.of(
        "eventId", "evt-2",
        "eventVersion", 1,
        "occurredAt", "2026-02-25T12:00:00Z",
        "encomendaId", 11L,
        "nomeDestinatario", "Joao",
        "apartamento", "102",
        "bloco", "B",
        "descricao", "Envelope",
        "recebidoPor", "porteiro",
        "status", "RECEBIDA"
    ));
    String payload = objectMapper.writeValueAsString(Map.of("payload", innerPayload));

    listener.consumir(payload);

    ArgumentCaptor<ProcessarEncomendaRecebidaPorUnidadeUseCase.Command> captor =
        ArgumentCaptor.forClass(ProcessarEncomendaRecebidaPorUnidadeUseCase.Command.class);
    verify(processarEncomendaRecebidaPorUnidadeUseCase).executar(captor.capture());
    assertEquals("evt-2", captor.getValue().eventId());
    assertEquals("B", captor.getValue().bloco());
  }

  @Test
  void deveConsumirEnvelopeComPayloadObjeto() throws Exception {
    String payload = objectMapper.writeValueAsString(Map.of(
        "payload", Map.of(
            "eventId", "evt-3",
            "eventVersion", 1,
            "occurredAt", "2026-02-25T12:00:00Z",
            "encomendaId", 12L,
            "nomeDestinatario", "Ana",
            "apartamento", "103",
            "bloco", "C",
            "descricao", "Pacote",
            "recebidoPor", "porteiro",
            "status", "RECEBIDA"
        )
    ));

    listener.consumir(payload);

    ArgumentCaptor<ProcessarEncomendaRecebidaPorUnidadeUseCase.Command> captor =
        ArgumentCaptor.forClass(ProcessarEncomendaRecebidaPorUnidadeUseCase.Command.class);
    verify(processarEncomendaRecebidaPorUnidadeUseCase).executar(captor.capture());
    assertEquals("evt-3", captor.getValue().eventId());
    assertEquals("Pacote", captor.getValue().descricao());
  }

  @Test
  void deveFalharQuandoPayloadForInvalido() {
    assertThrows(IllegalArgumentException.class, () -> listener.consumir("{payload-invalido"));
    verifyNoInteractions(processarEncomendaRecebidaPorUnidadeUseCase);
  }
}

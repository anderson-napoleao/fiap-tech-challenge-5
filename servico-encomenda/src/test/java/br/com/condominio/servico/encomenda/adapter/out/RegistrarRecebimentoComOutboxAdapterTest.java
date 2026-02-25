package br.com.condominio.servico.encomenda.adapter.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.encomenda.domain.Encomenda;
import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.EncomendaEntity;
import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.OutboxEventEntity;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataEncomendaRepository;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarRecebimentoComOutboxAdapterTest {

  @Mock
  private SpringDataEncomendaRepository encomendaRepository;

  @Mock
  private SpringDataOutboxEventRepository outboxEventRepository;

  @Mock
  private OutboxPayloadMapper outboxPayloadMapper;

  private RegistrarRecebimentoComOutboxAdapter adapter;

  @BeforeEach
  void setup() {
    Clock clock = Clock.fixed(Instant.parse("2026-02-25T10:00:00Z"), ZoneOffset.UTC);
    adapter = new RegistrarRecebimentoComOutboxAdapter(
        encomendaRepository,
        outboxEventRepository,
        outboxPayloadMapper,
        clock
    );
  }

  @Test
  void deveFalharQuandoEncomendaForNula() {
    assertThrows(IllegalArgumentException.class, () -> adapter.registrar(null));
  }

  @Test
  void deveRegistrarEncomendaEEventoOutbox() {
    Encomenda encomenda = Encomenda.receber(
        "Maria",
        "101",
        "A",
        "Caixa",
        "porteiro-1",
        Instant.parse("2026-02-25T09:59:00Z")
    );
    EncomendaEntity salva = new EncomendaEntity();
    salva.setId(1L);
    salva.setNomeDestinatario("Maria");
    salva.setApartamento("101");
    salva.setBloco("A");
    salva.setDescricao("Caixa");
    salva.setRecebidoPor("porteiro-1");
    salva.setStatus(StatusEncomenda.RECEBIDA);
    salva.setDataRecebimento(Instant.parse("2026-02-25T09:59:00Z"));

    when(encomendaRepository.save(any(EncomendaEntity.class))).thenReturn(salva);
    when(outboxPayloadMapper.toJson(any())).thenReturn("{\"ok\":true}");

    Encomenda result = adapter.registrar(encomenda);

    assertEquals(1L, result.id());
    assertEquals(StatusEncomenda.RECEBIDA, result.status());

    ArgumentCaptor<OutboxEventEntity> outboxCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);
    verify(outboxEventRepository).save(outboxCaptor.capture());
    assertEquals("ENCOMENDA", outboxCaptor.getValue().getAggregateType());
    assertEquals("1", outboxCaptor.getValue().getAggregateId());
    assertEquals("NotificacaoSolicitada".equals(outboxCaptor.getValue().getType()), false);
    assertEquals("EncomendaRecebida", outboxCaptor.getValue().getType());
  }

  @Test
  void deveReconstruirDominioComStatusRetirada() {
    Encomenda encomenda = Encomenda.receber(
        "Maria",
        "101",
        "A",
        "Caixa",
        "porteiro-1",
        Instant.parse("2026-02-25T09:00:00Z")
    );

    EncomendaEntity salva = new EncomendaEntity();
    salva.setId(1L);
    salva.setNomeDestinatario("Maria");
    salva.setApartamento("101");
    salva.setBloco("A");
    salva.setDescricao("Caixa");
    salva.setRecebidoPor("porteiro-1");
    salva.setStatus(StatusEncomenda.RETIRADA);
    salva.setDataRecebimento(Instant.parse("2026-02-25T09:00:00Z"));
    salva.setDataRetirada(Instant.parse("2026-02-25T10:00:00Z"));
    salva.setRetiradoPorNome("Maria");

    when(encomendaRepository.save(any(EncomendaEntity.class))).thenReturn(salva);
    when(outboxPayloadMapper.toJson(any())).thenReturn("{\"ok\":true}");

    Encomenda result = adapter.registrar(encomenda);

    assertEquals(StatusEncomenda.RETIRADA, result.status());
    assertEquals("Maria", result.retiradoPorNome());
    assertEquals(Instant.parse("2026-02-25T10:00:00Z"), result.dataRetirada());
  }
}


package br.com.condominio.servico.encomenda.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.encomenda.application.port.in.ReceberEncomendaUseCase;
import br.com.condominio.servico.encomenda.application.port.out.RegistrarRecebimentoComOutboxPort;
import br.com.condominio.servico.encomenda.domain.Encomenda;
import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReceberEncomendaServiceTest {

  @Mock
  private RegistrarRecebimentoComOutboxPort registrarRecebimentoComOutboxPort;

  private ReceberEncomendaService service;

  @BeforeEach
  void setup() {
    Clock clock = Clock.fixed(Instant.parse("2026-02-25T10:00:00Z"), ZoneOffset.UTC);
    service = new ReceberEncomendaService(registrarRecebimentoComOutboxPort, clock);
  }

  @Test
  void deveFalharQuandoComandoForNulo() {
    assertThrows(IllegalArgumentException.class, () -> service.executar(null));
  }

  @Test
  void deveRegistrarRecebimentoDaEncomenda() {
    when(registrarRecebimentoComOutboxPort.registrar(any(Encomenda.class)))
        .thenReturn(
            Encomenda.receber(
                    "Maria",
                    "101",
                    "A",
                    "Caixa",
                    "porteiro-1",
                    Instant.parse("2026-02-25T10:00:00Z")
                )
                .atribuirId(1L)
        );

    ReceberEncomendaUseCase.Result result = service.executar(
        new ReceberEncomendaUseCase.Command("Maria", "101", "A", "Caixa", "porteiro-1")
    );

    assertEquals(1L, result.id());
    assertEquals("Maria", result.nomeDestinatario());
    assertEquals(StatusEncomenda.RECEBIDA, result.status());
  }
}


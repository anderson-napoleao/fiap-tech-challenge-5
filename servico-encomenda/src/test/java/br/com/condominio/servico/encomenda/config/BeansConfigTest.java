package br.com.condominio.servico.encomenda.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import br.com.condominio.servico.encomenda.application.port.out.EncomendaRepositoryPort;
import br.com.condominio.servico.encomenda.application.port.out.RegistrarRecebimentoComOutboxPort;
import br.com.condominio.servico.encomenda.application.service.BaixarEncomendaRetiradaService;
import br.com.condominio.servico.encomenda.application.service.BuscarEncomendaPorIdService;
import br.com.condominio.servico.encomenda.application.service.ListarEncomendasPortariaService;
import br.com.condominio.servico.encomenda.application.service.ReceberEncomendaService;
import java.time.Clock;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BeansConfigTest {

  @Mock
  private RegistrarRecebimentoComOutboxPort registrarRecebimentoComOutboxPort;

  @Mock
  private EncomendaRepositoryPort encomendaRepositoryPort;

  @Test
  void deveCriarBeansDeAplicacao() {
    BeansConfig config = new BeansConfig();

    Clock clock = config.appClock();
    assertEquals(ZoneOffset.UTC, clock.getZone());

    assertInstanceOf(
        ReceberEncomendaService.class,
        config.receberEncomendaUseCase(registrarRecebimentoComOutboxPort, clock)
    );
    assertInstanceOf(
        BaixarEncomendaRetiradaService.class,
        config.baixarEncomendaRetiradaUseCase(encomendaRepositoryPort, clock)
    );
    assertInstanceOf(
        BuscarEncomendaPorIdService.class,
        config.buscarEncomendaPorIdUseCase(encomendaRepositoryPort)
    );
    assertInstanceOf(
        ListarEncomendasPortariaService.class,
        config.listarEncomendasPortariaUseCase(encomendaRepositoryPort)
    );
  }
}


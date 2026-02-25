package br.com.condominio.servico.encomenda.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.condominio.servico.encomenda.application.port.in.ListarEncomendasPortariaUseCase;
import br.com.condominio.servico.encomenda.application.port.in.ReceberEncomendaUseCase;
import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.EncomendaEntity;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataEncomendaRepository;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration")
@EnabledIfSystemProperty(named = "testcontainers.enabled", matches = "true")
class ListarEncomendasPortariaIntegrationTest {

  @Autowired
  private ReceberEncomendaUseCase receberEncomendaUseCase;

  @Autowired
  private ListarEncomendasPortariaUseCase listarEncomendasPortariaUseCase;

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
  void deveListarEncomendasComPaginacaoEFiltros() {
    receberEncomendaUseCase.executar(
        new ReceberEncomendaUseCase.Command("Maria", "101", "A", "Caixa 1", "porteiro-1")
    );
    receberEncomendaUseCase.executar(
        new ReceberEncomendaUseCase.Command("Joao", "101", "A", "Caixa 2", "porteiro-1")
    );
    receberEncomendaUseCase.executar(
        new ReceberEncomendaUseCase.Command("Ana", "202", "B", "Caixa 3", "porteiro-1")
    );

    List<EncomendaEntity> entidades = encomendaRepository.findAll().stream()
        .sorted(Comparator.comparing(EncomendaEntity::getId))
        .toList();
    EncomendaEntity primeira = entidades.getFirst();
    EncomendaEntity segunda = entidades.get(1);
    EncomendaEntity terceira = entidades.get(2);

    primeira.setDataRecebimento(Instant.parse("2026-02-24T10:00:00Z"));
    segunda.setDataRecebimento(Instant.parse("2026-02-24T12:00:00Z"));
    terceira.setDataRecebimento(Instant.parse("2026-02-25T09:00:00Z"));
    encomendaRepository.saveAll(List.of(primeira, segunda, terceira));

    ListarEncomendasPortariaUseCase.Result paginaCompleta = listarEncomendasPortariaUseCase.executar(
        new ListarEncomendasPortariaUseCase.Command(null, null, null, 0, 2)
    );
    assertEquals(2, paginaCompleta.encomendas().size());
    assertEquals(3, paginaCompleta.totalElements());
    assertEquals(2, paginaCompleta.totalPages());

    ListarEncomendasPortariaUseCase.Result filtrada = listarEncomendasPortariaUseCase.executar(
        new ListarEncomendasPortariaUseCase.Command("101", "a", null, 0, 10)
    );
    assertEquals(2, filtrada.encomendas().size());
    assertEquals(2, filtrada.totalElements());
    assertEquals(1, filtrada.totalPages());
    assertTrue(filtrada.encomendas().stream().allMatch(item -> "101".equals(item.apartamento())));
    assertTrue(filtrada.encomendas().stream().allMatch(item -> "A".equals(item.bloco())));

    ListarEncomendasPortariaUseCase.Result filtradaPorData = listarEncomendasPortariaUseCase.executar(
        new ListarEncomendasPortariaUseCase.Command(null, null, LocalDate.parse("2026-02-24"), 0, 10)
    );
    assertEquals(2, filtradaPorData.encomendas().size());
    assertEquals(2, filtradaPorData.totalElements());
    assertEquals(1, filtradaPorData.totalPages());
  }
}

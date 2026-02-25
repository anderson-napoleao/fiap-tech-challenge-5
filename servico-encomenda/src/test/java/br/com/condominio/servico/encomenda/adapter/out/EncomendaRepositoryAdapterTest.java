package br.com.condominio.servico.encomenda.adapter.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.encomenda.application.port.out.EncomendaRepositoryPort;
import br.com.condominio.servico.encomenda.domain.Encomenda;
import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.EncomendaEntity;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataEncomendaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class EncomendaRepositoryAdapterTest {

  @Mock
  private SpringDataEncomendaRepository repository;

  private EncomendaRepositoryAdapter adapter;

  @BeforeEach
  void setup() {
    adapter = new EncomendaRepositoryAdapter(repository);
  }

  @Test
  void deveFalharAoSalvarQuandoEncomendaForNula() {
    assertThrows(IllegalArgumentException.class, () -> adapter.salvar(null));
  }

  @Test
  void deveSalvarEncomenda() {
    Encomenda encomenda = Encomenda.receber(
            "Maria",
            "101",
            "A",
            "Caixa",
            "porteiro-1",
            Instant.parse("2026-02-25T10:00:00Z")
        )
        .atribuirId(1L);
    EncomendaEntity entity = toEntity(encomenda);
    when(repository.save(any(EncomendaEntity.class))).thenReturn(entity);

    Encomenda salva = adapter.salvar(encomenda);

    ArgumentCaptor<EncomendaEntity> captor = ArgumentCaptor.forClass(EncomendaEntity.class);
    verify(repository).save(captor.capture());
    assertEquals("101", captor.getValue().getApartamento());
    assertEquals(1L, salva.id());
    assertEquals(StatusEncomenda.RECEBIDA, salva.status());
  }

  @Test
  void deveBuscarPorId() {
    Encomenda encomenda = Encomenda.receber(
            "Maria",
            "101",
            "A",
            "Caixa",
            "porteiro-1",
            Instant.parse("2026-02-25T10:00:00Z")
        )
        .atribuirId(1L);
    when(repository.findById(1L)).thenReturn(Optional.of(toEntity(encomenda)));

    Optional<Encomenda> encontrada = adapter.buscarPorId(1L);

    assertTrue(encontrada.isPresent());
    assertEquals("Maria", encontrada.orElseThrow().nomeDestinatario());
  }

  @Test
  void deveListarEncomendasComPaginacao() {
    EncomendaEntity entity = new EncomendaEntity();
    entity.setId(1L);
    entity.setNomeDestinatario("Maria");
    entity.setApartamento("101");
    entity.setBloco("A");
    entity.setDescricao("Caixa");
    entity.setRecebidoPor("porteiro-1");
    entity.setStatus(StatusEncomenda.RECEBIDA);
    entity.setDataRecebimento(Instant.parse("2026-02-25T10:00:00Z"));

    Page<EncomendaEntity> page = new PageImpl<>(List.of(entity), Pageable.ofSize(10), 1);
    when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

    EncomendaRepositoryPort.ResultadoListagem resultado = adapter.listar(
        new EncomendaRepositoryPort.FiltroListagem("101", "a", LocalDate.parse("2026-02-25"), 0, 10)
    );

    assertEquals(1, resultado.encomendas().size());
    assertEquals("A", resultado.encomendas().getFirst().bloco());
    assertEquals(1, resultado.totalPages());
  }

  @Test
  void deveMapearStatusRetiradaNaListagem() {
    EncomendaEntity entity = new EncomendaEntity();
    entity.setId(1L);
    entity.setNomeDestinatario("Maria");
    entity.setApartamento("101");
    entity.setBloco("A");
    entity.setDescricao("Caixa");
    entity.setRecebidoPor("porteiro-1");
    entity.setStatus(StatusEncomenda.RETIRADA);
    entity.setDataRecebimento(Instant.parse("2026-02-25T10:00:00Z"));
    entity.setDataRetirada(Instant.parse("2026-02-25T11:00:00Z"));
    entity.setRetiradoPorNome("Maria");

    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity), Pageable.ofSize(10), 1));

    EncomendaRepositoryPort.ResultadoListagem resultado = adapter.listar(
        new EncomendaRepositoryPort.FiltroListagem(null, null, null, 0, 10)
    );

    assertEquals(StatusEncomenda.RETIRADA, resultado.encomendas().getFirst().status());
    assertEquals("Maria", resultado.encomendas().getFirst().retiradoPorNome());
  }

  @Test
  void deveValidarParametrosDaListagem() {
    assertThrows(IllegalArgumentException.class, () -> adapter.listar(null));
    assertThrows(
        IllegalArgumentException.class,
        () -> adapter.listar(new EncomendaRepositoryPort.FiltroListagem(null, null, null, -1, 10))
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> adapter.listar(new EncomendaRepositoryPort.FiltroListagem(null, null, null, 0, 0))
    );
  }

  private EncomendaEntity toEntity(Encomenda encomenda) {
    EncomendaEntity entity = new EncomendaEntity();
    entity.setId(encomenda.id());
    entity.setNomeDestinatario(encomenda.nomeDestinatario());
    entity.setApartamento(encomenda.apartamento());
    entity.setBloco(encomenda.bloco());
    entity.setDescricao(encomenda.descricao());
    entity.setRecebidoPor(encomenda.recebidoPor());
    entity.setStatus(eqOrDefault(encomenda.status()));
    entity.setDataRecebimento(encomenda.dataRecebimento());
    entity.setDataRetirada(encomenda.dataRetirada());
    entity.setRetiradoPorNome(encomenda.retiradoPorNome());
    return entity;
  }

  private StatusEncomenda eqOrDefault(StatusEncomenda status) {
    return status == null ? StatusEncomenda.RECEBIDA : status;
  }
}


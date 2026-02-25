package br.com.condominio.servico.encomenda.adapter.out;

import br.com.condominio.servico.encomenda.application.port.out.EncomendaRepositoryPort;
import br.com.condominio.servico.encomenda.domain.Encomenda;
import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.EncomendaEntity;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataEncomendaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de saida para persistencia ou integracao externa.
 */
@Component
public class EncomendaRepositoryAdapter implements EncomendaRepositoryPort {

  private final SpringDataEncomendaRepository encomendaRepository;

  public EncomendaRepositoryAdapter(SpringDataEncomendaRepository encomendaRepository) {
    this.encomendaRepository = encomendaRepository;
  }

  @Override
  public Optional<Encomenda> buscarPorId(Long id) {
    return encomendaRepository.findById(id).map(this::toDomain);
  }

  @Override
  @Transactional
  public Encomenda salvar(Encomenda encomenda) {
    if (encomenda == null) {
      throw new IllegalArgumentException("Encomenda obrigatoria");
    }
    return toDomain(encomendaRepository.save(toEntity(encomenda)));
  }

  @Override
  public ResultadoListagem listar(FiltroListagem filtro) {
    if (filtro == null) {
      throw new IllegalArgumentException("Filtro obrigatorio");
    }
    if (filtro.page() < 0) {
      throw new IllegalArgumentException("Page invalida");
    }
    if (filtro.size() <= 0) {
      throw new IllegalArgumentException("Size invalido");
    }

    Page<EncomendaEntity> page = encomendaRepository.findByFiltros(
        normalizarFiltro(filtro.apartamento()),
        normalizarFiltro(filtro.bloco()),
        inicioDoDiaUtc(filtro.data()),
        fimDoDiaUtc(filtro.data()),
        PageRequest.of(filtro.page(), filtro.size(), Sort.by(Sort.Direction.DESC, "dataRecebimento"))
    );

    return new ResultadoListagem(
        page.stream().map(this::toDomain).toList(),
        page.getTotalElements(),
        page.getTotalPages()
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
    entity.setStatus(encomenda.status());
    entity.setDataRecebimento(encomenda.dataRecebimento());
    entity.setDataRetirada(encomenda.dataRetirada());
    entity.setRetiradoPorNome(encomenda.retiradoPorNome());
    return entity;
  }

  private Encomenda toDomain(EncomendaEntity entity) {
    Encomenda encomenda = Encomenda.receber(
        entity.getNomeDestinatario(),
        entity.getApartamento(),
        entity.getBloco(),
        entity.getDescricao(),
        entity.getRecebidoPor(),
        entity.getDataRecebimento()
    ).atribuirId(entity.getId());

    if (entity.getStatus() == StatusEncomenda.RETIRADA) {
      Instant dataRetirada = entity.getDataRetirada();
      encomenda.marcarRetirada(dataRetirada, entity.getRetiradoPorNome());
    }

    return encomenda;
  }

  private String normalizarFiltro(String valor) {
    if (valor == null) {
      return null;
    }
    String normalizado = valor.trim();
    return normalizado.isEmpty() ? null : normalizado.toUpperCase(Locale.ROOT);
  }

  private Instant inicioDoDiaUtc(LocalDate data) {
    if (data == null) {
      return null;
    }
    return data.atStartOfDay().toInstant(ZoneOffset.UTC);
  }

  private Instant fimDoDiaUtc(LocalDate data) {
    if (data == null) {
      return null;
    }
    return data.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
  }
}

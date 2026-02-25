package br.com.condominio.servico.encomenda.adapter.out;

import br.com.condominio.servico.encomenda.application.port.out.EncomendaRepositoryPort;
import br.com.condominio.servico.encomenda.domain.Encomenda;
import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.EncomendaEntity;
import br.com.condominio.servico.encomenda.infrastructure.persistence.repository.SpringDataEncomendaRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
}

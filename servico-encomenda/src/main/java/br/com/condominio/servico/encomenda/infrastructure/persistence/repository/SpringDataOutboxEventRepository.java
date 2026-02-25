package br.com.condominio.servico.encomenda.infrastructure.persistence.repository;

import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.OutboxEventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data para acesso a persistencia.
 */
public interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEventEntity, String> {

  List<OutboxEventEntity> findByAggregateId(String aggregateId);
}

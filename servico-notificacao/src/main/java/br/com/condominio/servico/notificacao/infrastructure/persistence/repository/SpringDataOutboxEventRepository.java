package br.com.condominio.servico.notificacao.infrastructure.persistence.repository;

import br.com.condominio.servico.notificacao.infrastructure.persistence.entity.OutboxEventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEventEntity, String> {

  List<OutboxEventEntity> findByAggregateId(String aggregateId);
}

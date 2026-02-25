package br.com.condominio.servico.encomenda.infrastructure.persistence.repository;

import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.EncomendaEntity;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data para acesso a persistencia.
 */
public interface SpringDataEncomendaRepository extends JpaRepository<EncomendaEntity, Long> {

  @Query("""
      SELECT e
      FROM EncomendaEntity e
      WHERE (:apartamento IS NULL OR UPPER(e.apartamento) = UPPER(:apartamento))
        AND (:bloco IS NULL OR UPPER(e.bloco) = UPPER(:bloco))
        AND (:dataInicio IS NULL OR e.dataRecebimento >= :dataInicio)
        AND (:dataFim IS NULL OR e.dataRecebimento < :dataFim)
      """)
  Page<EncomendaEntity> findByFiltros(
      @Param("apartamento") String apartamento,
      @Param("bloco") String bloco,
      @Param("dataInicio") Instant dataInicio,
      @Param("dataFim") Instant dataFim,
      Pageable pageable
  );
}

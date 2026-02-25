package br.com.condominio.servico.encomenda.infrastructure.persistence.repository;

import br.com.condominio.servico.encomenda.infrastructure.persistence.entity.EncomendaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositorio Spring Data para acesso a persistencia.
 */
public interface SpringDataEncomendaRepository
    extends JpaRepository<EncomendaEntity, Long>, JpaSpecificationExecutor<EncomendaEntity> {
}

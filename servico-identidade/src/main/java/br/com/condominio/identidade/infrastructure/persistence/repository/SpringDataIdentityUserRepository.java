package br.com.condominio.identidade.infrastructure.persistence.repository;

import br.com.condominio.identidade.infrastructure.persistence.entity.IdentityUserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Define o contrato de saida da aplicacao para integracoes externas.
 */
public interface SpringDataIdentityUserRepository extends JpaRepository<IdentityUserEntity, String> {

  boolean existsByUsername(String username);

  Optional<IdentityUserEntity> findByUsername(String username);
}

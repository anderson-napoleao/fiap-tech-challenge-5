package br.com.condominio.servico.usuario.infrastructure.persistence.repository;

import br.com.condominio.servico.usuario.infrastructure.persistence.entity.UsuarioEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data para acesso a persistencia.
 */
public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

  Optional<UsuarioEntity> findByIdentityId(String identityId);

  boolean existsByEmail(String email);
}

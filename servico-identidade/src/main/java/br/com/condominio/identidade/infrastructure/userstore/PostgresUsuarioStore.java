package br.com.condominio.identidade.infrastructure.userstore;

import br.com.condominio.identidade.application.port.out.UsuarioStorePort;
import br.com.condominio.identidade.infrastructure.persistence.entity.IdentityUserEntity;
import br.com.condominio.identidade.infrastructure.persistence.repository.SpringDataIdentityUserRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa armazenamento de usuarios para a infraestrutura de identidade.
 */
@Component
public class PostgresUsuarioStore implements UsuarioStorePort {

  private final SpringDataIdentityUserRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;

  public PostgresUsuarioStore(
      SpringDataIdentityUserRepository usuarioRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByUsername(String username) {
    return usuarioRepository.existsByUsername(username);
  }

  @Override
  @Transactional
  public void save(String username, String rawPassword, Set<String> roles) {
    Optional<IdentityUserEntity> existente = usuarioRepository.findByUsername(username);
    String identityId = existente.map(IdentityUserEntity::getId).orElse(UUID.randomUUID().toString());

    Set<String> normalizedRoles = normalizeRoles(roles);
    IdentityUserEntity entity = existente.orElseGet(IdentityUserEntity::new);
    entity.setId(identityId);
    entity.setUsername(username);
    entity.setPasswordHash(passwordEncoder.encode(rawPassword));
    entity.setEnabled(true);
    entity.setRoles(new HashSet<>(normalizedRoles));

    usuarioRepository.save(entity);
  }

  @Override
  @Transactional
  public IdentityUserData create(String email, String rawPassword, String role) {
    if (existsByEmail(email)) {
      throw new IllegalArgumentException("usuario ja existe");
    }

    String identityId = UUID.randomUUID().toString();
    Set<String> roles = Set.of(toRoleAuthority(role));

    IdentityUserEntity entity = new IdentityUserEntity();
    entity.setId(identityId);
    entity.setUsername(email);
    entity.setPasswordHash(passwordEncoder.encode(rawPassword));
    entity.setEnabled(true);
    entity.setRoles(new HashSet<>(roles));
    usuarioRepository.save(entity);

    return new IdentityUserData(identityId, email, true, roles);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<IdentityUserData> findByUsername(String username) {
    return usuarioRepository.findByUsername(username)
        .map(user -> new IdentityUserData(
            user.getId(),
            user.getUsername(),
            user.isEnabled(),
            Set.copyOf(user.getRoles())
        ));
  }

  @Override
  @Transactional(readOnly = true)
  public boolean matchesPassword(String username, String rawPassword) {
    return usuarioRepository.findByUsername(username)
        .map(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()))
        .orElse(false);
  }

  @Override
  @Transactional
  public void removeById(String identityId) {
    if (!usuarioRepository.existsById(identityId)) {
      return;
    }
    usuarioRepository.deleteById(identityId);
  }

  @Override
  @Transactional
  public void disableById(String identityId) {
    usuarioRepository.findById(identityId).ifPresent(user -> {
      user.setEnabled(false);
      usuarioRepository.save(user);
    });
  }

  private Set<String> normalizeRoles(Set<String> roles) {
    if (roles == null || roles.isEmpty()) {
      throw new IllegalArgumentException("roles nao pode ser vazio");
    }
    return roles.stream()
        .map(this::toRoleAuthority)
        .collect(Collectors.toUnmodifiableSet());
  }

  private String toRoleAuthority(String role) {
    String clean = role == null ? "" : role.trim().toUpperCase();
    if (clean.startsWith("ROLE_")) {
      return clean;
    }
    return "ROLE_" + clean;
  }
}

package br.com.condominio.identidade.infrastructure.userstore;

import br.com.condominio.identidade.application.port.out.UsuarioStorePort;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InMemoryUsuarioStore implements UsuarioStorePort {

  private final ConcurrentMap<String, UsuarioData> usuariosPorUsername = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, String> usernamePorId = new ConcurrentHashMap<>();
  private final PasswordEncoder passwordEncoder;

  public InMemoryUsuarioStore(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public boolean existsByUsername(String username) {
    return usuariosPorUsername.containsKey(username);
  }

  @Override
  public void save(String username, String rawPassword, Set<String> roles) {
    Set<String> normalizedRoles =
        roles.stream().map(this::toRoleAuthority).collect(java.util.stream.Collectors.toUnmodifiableSet());

    String identityId = usuariosPorUsername.containsKey(username)
        ? usuariosPorUsername.get(username).id()
        : UUID.randomUUID().toString();

    saveInternal(identityId, username, rawPassword, normalizedRoles, true);
  }

  @Override
  public IdentityUserData create(String email, String rawPassword, String role) {
    if (existsByEmail(email)) {
      throw new IllegalArgumentException("usuario ja existe");
    }

    String identityId = UUID.randomUUID().toString();
    Set<String> roles = Set.of(toRoleAuthority(role));
    saveInternal(identityId, email, rawPassword, roles, true);

    return new IdentityUserData(identityId, email, true, roles);
  }

  @Override
  public void removeById(String identityId) {
    String username = usernamePorId.remove(identityId);
    if (username == null) {
      return;
    }
    usuariosPorUsername.remove(username);
  }

  @Override
  public void disableById(String identityId) {
    String username = usernamePorId.get(identityId);
    if (username == null) {
      return;
    }

    usuariosPorUsername.computeIfPresent(
        username,
        (key, existente) -> new UsuarioData(
            existente.id(),
            existente.username(),
            existente.passwordEncoded(),
            existente.roles(),
            false
        )
    );
  }

  public Optional<UsuarioData> findByUsername(String username) {
    return Optional.ofNullable(usuariosPorUsername.get(username));
  }

  private void saveInternal(
      String identityId,
      String username,
      String rawPassword,
      Set<String> roles,
      boolean enabled
  ) {
    UsuarioData anterior = usuariosPorUsername.put(
        username,
        new UsuarioData(identityId, username, passwordEncoder.encode(rawPassword), roles, enabled)
    );

    if (anterior != null && !anterior.id().equals(identityId)) {
      usernamePorId.remove(anterior.id());
    }

    usernamePorId.put(identityId, username);
  }

  private String toRoleAuthority(String role) {
    String clean = role == null ? "" : role.trim().toUpperCase();
    if (clean.startsWith("ROLE_")) {
      return clean;
    }
    return "ROLE_" + clean;
  }

  public record UsuarioData(
      String id,
      String username,
      String passwordEncoded,
      Set<String> roles,
      boolean enabled
  ) {
  }
}

package br.com.condominio.identidade.infrastructure.userstore;

import br.com.condominio.identidade.application.port.out.UsuarioStorePort;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InMemoryUsuarioStore implements UsuarioStorePort {

  private final ConcurrentMap<String, UsuarioData> usuarios = new ConcurrentHashMap<>();
  private final PasswordEncoder passwordEncoder;

  public InMemoryUsuarioStore(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public boolean existsByUsername(String username) {
    return usuarios.containsKey(username);
  }

  @Override
  public void save(String username, String rawPassword, Set<String> roles) {
    Set<String> normalizedRoles =
        roles.stream().map(this::toRoleAuthority).collect(java.util.stream.Collectors.toUnmodifiableSet());

    usuarios.put(
        username,
        new UsuarioData(username, passwordEncoder.encode(rawPassword), normalizedRoles));
  }

  public Optional<UsuarioData> findByUsername(String username) {
    return Optional.ofNullable(usuarios.get(username));
  }

  private String toRoleAuthority(String role) {
    String clean = role == null ? "" : role.trim().toUpperCase();
    if (clean.startsWith("ROLE_")) {
      return clean;
    }
    return "ROLE_" + clean;
  }

  public record UsuarioData(String username, String passwordEncoded, Set<String> roles) {}
}

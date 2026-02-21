package br.com.condominio.identidade.application.port.out;

import java.util.Set;

public interface UsuarioStorePort {

  record IdentityUserData(String id, String email, boolean enabled, Set<String> roles) {
  }

  boolean existsByUsername(String username);

  void save(String username, String rawPassword, Set<String> roles);

  default boolean existsByEmail(String email) {
    return existsByUsername(email);
  }

  IdentityUserData create(String email, String rawPassword, String role);

  void removeById(String identityId);

  void disableById(String identityId);
}

package br.com.condominio.identidade.application.port.out;

import java.util.Set;

public interface UsuarioStorePort {

  boolean existsByUsername(String username);

  void save(String username, String rawPassword, Set<String> roles);
}

package br.com.condominio.identidade.application.service;

import br.com.condominio.identidade.application.port.in.CriarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.out.UsuarioStorePort;
import java.util.List;
import java.util.Set;

public class CriarUsuarioAdminService implements CriarUsuarioAdminUseCase {

  private final UsuarioStorePort usuarioStorePort;

  public CriarUsuarioAdminService(UsuarioStorePort usuarioStorePort) {
    this.usuarioStorePort = usuarioStorePort;
  }

  @Override
  public UsuarioResponse criar(String username, String password, List<String> roles) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username nao pode ser vazio");
    }
    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("password nao pode ser vazio");
    }
    if (roles == null || roles.isEmpty()) {
      throw new IllegalArgumentException("roles nao pode ser vazio");
    }
    if (usuarioStorePort.existsByUsername(username)) {
      throw new IllegalArgumentException("usuario ja existe");
    }

    usuarioStorePort.save(username, password, Set.copyOf(roles));
    return new UsuarioResponse(username, List.copyOf(roles));
  }
}

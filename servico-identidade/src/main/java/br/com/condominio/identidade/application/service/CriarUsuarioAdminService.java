package br.com.condominio.identidade.application.service;

import br.com.condominio.identidade.application.port.in.CriarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.out.UsuarioStorePort;

/**
 * Implementa a orquestracao de regras da camada de aplicacao.
 */
public class CriarUsuarioAdminService implements CriarUsuarioAdminUseCase {

  private final UsuarioStorePort usuarioStorePort;

  public CriarUsuarioAdminService(UsuarioStorePort usuarioStorePort) {
    this.usuarioStorePort = usuarioStorePort;
  }

  @Override
  public UsuarioResponse criar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("comando nao pode ser nulo");
    }
    if (usuarioStorePort.existsByEmail(command.email())) {
      throw new IllegalArgumentException("usuario ja existe");
    }

    UsuarioStorePort.IdentityUserData criado =
        usuarioStorePort.create(command.email(), command.password(), command.role());

    return new UsuarioResponse(criado.id(), criado.email(), command.role());
  }
}

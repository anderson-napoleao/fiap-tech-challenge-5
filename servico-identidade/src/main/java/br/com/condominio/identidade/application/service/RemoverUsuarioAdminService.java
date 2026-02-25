package br.com.condominio.identidade.application.service;

import br.com.condominio.identidade.application.port.in.RemoverUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.out.UsuarioStorePort;

/**
 * Implementa a orquestracao de regras da camada de aplicacao.
 */
public class RemoverUsuarioAdminService implements RemoverUsuarioAdminUseCase {

  private final UsuarioStorePort usuarioStorePort;

  public RemoverUsuarioAdminService(UsuarioStorePort usuarioStorePort) {
    this.usuarioStorePort = usuarioStorePort;
  }

  @Override
  public void remover(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("comando nao pode ser nulo");
    }
    usuarioStorePort.removeById(command.identityId());
  }
}

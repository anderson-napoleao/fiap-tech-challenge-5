package br.com.condominio.identidade.application.service;

import br.com.condominio.identidade.application.port.in.DesabilitarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.out.UsuarioStorePort;

/**
 * Implementa a orquestracao de regras da camada de aplicacao.
 */
public class DesabilitarUsuarioAdminService implements DesabilitarUsuarioAdminUseCase {

  private final UsuarioStorePort usuarioStorePort;

  public DesabilitarUsuarioAdminService(UsuarioStorePort usuarioStorePort) {
    this.usuarioStorePort = usuarioStorePort;
  }

  @Override
  public void desabilitar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("comando nao pode ser nulo");
    }
    usuarioStorePort.disableById(command.identityId());
  }
}

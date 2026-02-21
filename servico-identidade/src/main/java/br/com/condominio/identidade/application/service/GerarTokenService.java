package br.com.condominio.identidade.application.service;

import br.com.condominio.identidade.application.exception.CredenciaisInvalidasException;
import br.com.condominio.identidade.application.port.in.GerarTokenUseCase;
import br.com.condominio.identidade.application.port.out.TokenJwtPort;
import br.com.condominio.identidade.application.port.out.UsuarioStorePort;

public class GerarTokenService implements GerarTokenUseCase {

  private final UsuarioStorePort usuarioStorePort;
  private final TokenJwtPort tokenJwtPort;

  public GerarTokenService(UsuarioStorePort usuarioStorePort, TokenJwtPort tokenJwtPort) {
    this.usuarioStorePort = usuarioStorePort;
    this.tokenJwtPort = tokenJwtPort;
  }

  @Override
  public Result gerar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("command obrigatorio");
    }

    UsuarioStorePort.IdentityUserData usuario = usuarioStorePort.findByUsername(command.username())
        .orElseThrow(CredenciaisInvalidasException::new);

    if (!usuario.enabled()) {
      throw new CredenciaisInvalidasException();
    }

    if (!usuarioStorePort.matchesPassword(command.username(), command.password())) {
      throw new CredenciaisInvalidasException();
    }

    TokenJwtPort.TokenGerado tokenGerado = tokenJwtPort.gerarToken(usuario);
    return new Result(tokenGerado.accessToken(), tokenGerado.tokenType(), tokenGerado.expiresIn());
  }
}

package br.com.condominio.identidade.application.port.out;

public interface TokenJwtPort {

  record TokenGerado(String accessToken, String tokenType, long expiresIn) {
  }

  TokenGerado gerarToken(UsuarioStorePort.IdentityUserData usuario);
}

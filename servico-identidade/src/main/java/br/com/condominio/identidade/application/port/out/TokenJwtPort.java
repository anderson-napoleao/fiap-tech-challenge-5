package br.com.condominio.identidade.application.port.out;

/**
 * Define o contrato de saida da aplicacao para integracoes externas.
 */
public interface TokenJwtPort {

  record TokenGerado(String accessToken, String tokenType, long expiresIn) {
  }

  TokenGerado gerarToken(UsuarioStorePort.IdentityUserData usuario);
}

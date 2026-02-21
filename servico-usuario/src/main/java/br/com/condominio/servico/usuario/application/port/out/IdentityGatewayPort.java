package br.com.condominio.servico.usuario.application.port.out;

public interface IdentityGatewayPort {

  record CriarIdentidadeCommand(String email, String senha, String role) {
  }

  record CriarIdentidadeResult(String identityId, String email) {
  }

  CriarIdentidadeResult criarUsuario(CriarIdentidadeCommand command);

  void removerUsuario(String identityId);

  void desabilitarUsuario(String identityId);
}

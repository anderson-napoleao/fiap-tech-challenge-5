package br.com.condominio.servico.usuario.application.port.in;

import br.com.condominio.servico.usuario.domain.TipoUsuario;

public interface ObterMeuPerfilUseCase {

  record Result(
      Long id,
      String identityId,
      String nomeCompleto,
      String email,
      TipoUsuario tipo,
      String telefone,
      String cpf,
      String apartamento,
      String bloco
  ) {
  }

  Result executar(String identityId);
}

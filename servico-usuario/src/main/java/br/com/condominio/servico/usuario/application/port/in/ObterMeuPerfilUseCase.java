package br.com.condominio.servico.usuario.application.port.in;

import br.com.condominio.servico.usuario.domain.TipoUsuario;

public interface ObterMeuPerfilUseCase {

  record Command(String identityId) {
    public Command {
      if (identityId == null || identityId.isBlank()) {
        throw new IllegalArgumentException("IdentityId obrigatorio");
      }
    }
  }

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

  Result executar(Command command);
}

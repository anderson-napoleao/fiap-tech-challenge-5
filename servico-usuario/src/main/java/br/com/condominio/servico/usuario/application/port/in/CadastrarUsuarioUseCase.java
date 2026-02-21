package br.com.condominio.servico.usuario.application.port.in;

import br.com.condominio.servico.usuario.domain.TipoUsuario;

public interface CadastrarUsuarioUseCase {

  record Command(
      String nomeCompleto,
      String email,
      String senha,
      TipoUsuario tipo,
      String telefone,
      String cpf,
      String apartamento,
      String bloco
  ) {
    public Command {
      // Validações estruturais apenas - sem regras de negócio
      if (nomeCompleto == null || nomeCompleto.isBlank()) {
        throw new IllegalArgumentException("Nome completo obrigatorio");
      }
      if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Email obrigatorio");
      }
      if (senha == null || senha.isBlank()) {
        throw new IllegalArgumentException("Senha obrigatoria");
      }
      if (tipo == null) {
        throw new IllegalArgumentException("Tipo obrigatorio");
      }
      // Regras de negócio ficam na entidade Usuario (Domínio Rico)
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

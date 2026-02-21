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
      if (nomeCompleto == null || nomeCompleto.isBlank()) {
        throw new IllegalArgumentException("Nome completo obrigatorio");
      }
      if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Email obrigatorio");
      }
      if (!email.contains("@")) {
        throw new IllegalArgumentException("Email invalido");
      }
      if (senha == null || senha.isBlank()) {
        throw new IllegalArgumentException("Senha obrigatoria");
      }
      if (tipo == null) {
        throw new IllegalArgumentException("Tipo obrigatorio");
      }
      validarOpcionalNaoVazio(telefone, "Telefone");
      validarOpcionalNaoVazio(cpf, "Cpf");
      validarOpcionalNaoVazio(apartamento, "Apartamento");
      validarOpcionalNaoVazio(bloco, "Bloco");
    }

    private static void validarOpcionalNaoVazio(String valor, String campo) {
      if (valor != null && valor.isBlank()) {
        throw new IllegalArgumentException(campo + " invalido");
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

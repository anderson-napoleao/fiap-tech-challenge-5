package br.com.condominio.servico.usuario.application.port.in;

import br.com.condominio.servico.usuario.domain.TipoUsuario;

public interface AtualizarMeuPerfilUseCase {

  record Command(
      String identityId,
      String nomeCompleto,
      String telefone,
      String cpf,
      String apartamento,
      String bloco
  ) {
    public Command {
      if (identityId == null || identityId.isBlank()) {
        throw new IllegalArgumentException("IdentityId obrigatorio");
      }
      validarOpcionalNaoVazio(nomeCompleto, "Nome completo");
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

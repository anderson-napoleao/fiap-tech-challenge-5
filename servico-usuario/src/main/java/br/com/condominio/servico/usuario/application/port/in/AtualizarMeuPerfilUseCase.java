package br.com.condominio.servico.usuario.application.port.in;

import br.com.condominio.servico.usuario.domain.TipoUsuario;

public interface AtualizarMeuPerfilUseCase {

  record Command(
      String nomeCompleto,
      String telefone,
      String cpf,
      String apartamento,
      String bloco
  ) {
    public Command {
      // Para atualização, os campos são opcionais, mas se fornecidos devem ser válidos
      // Validação específica por tipo será feita no service junto com o usuário existente
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

  Result executar(String identityId, Command command);
}

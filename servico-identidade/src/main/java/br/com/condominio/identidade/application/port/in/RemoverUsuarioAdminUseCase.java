package br.com.condominio.identidade.application.port.in;

/**
 * Define o contrato de entrada (use case) da aplicacao.
 */
public interface RemoverUsuarioAdminUseCase {

  record Command(String identityId) {
    public Command {
      if (identityId == null || identityId.isBlank()) {
        throw new IllegalArgumentException("identityId obrigatorio");
      }
    }
  }

  void remover(Command command);
}

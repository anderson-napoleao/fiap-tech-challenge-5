package br.com.condominio.identidade.application.port.in;

public interface DesabilitarUsuarioAdminUseCase {

  record Command(String identityId) {
    public Command {
      if (identityId == null || identityId.isBlank()) {
        throw new IllegalArgumentException("identityId obrigatorio");
      }
    }
  }

  void desabilitar(Command command);
}

package br.com.condominio.identidade.application.port.in;

public interface GerarTokenUseCase {

  record Command(String username, String password) {
    public Command {
      if (username == null || username.isBlank()) {
        throw new IllegalArgumentException("username obrigatorio");
      }
      if (password == null || password.isBlank()) {
        throw new IllegalArgumentException("password obrigatorio");
      }
    }
  }

  record Result(String accessToken, String tokenType, long expiresIn) {
  }

  Result gerar(Command command);
}

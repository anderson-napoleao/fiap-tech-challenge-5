package br.com.condominio.identidade.application.port.in;

import java.util.List;

public interface CriarUsuarioAdminUseCase {

  record Command(String email, String password, String role) {
    public Command {
      if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("email nao pode ser vazio");
      }
      if (!email.contains("@")) {
        throw new IllegalArgumentException("email invalido");
      }
      if (password == null || password.isBlank()) {
        throw new IllegalArgumentException("password nao pode ser vazio");
      }
      if (role == null || role.isBlank()) {
        throw new IllegalArgumentException("role nao pode ser vazio");
      }
    }

    public Command(String username, String password, List<String> roles) {
      this(username, password, firstRole(roles));
    }

    public String username() {
      return email;
    }

    public List<String> roles() {
      return List.of(role);
    }

    private static String firstRole(List<String> roles) {
      if (roles == null || roles.isEmpty()) {
        throw new IllegalArgumentException("roles nao pode ser vazio");
      }
      if (roles.stream().anyMatch(value -> value == null || value.isBlank())) {
        throw new IllegalArgumentException("roles contem valor invalido");
      }
      return roles.getFirst();
    }
  }

  UsuarioResponse criar(Command command);

  record UsuarioResponse(String id, String email, String role) {
    public String username() {
      return email;
    }

    public List<String> roles() {
      return List.of(role);
    }
  }
}

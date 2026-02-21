package br.com.condominio.identidade.application.port.in;

import java.util.List;

public interface CriarUsuarioAdminUseCase {

  UsuarioResponse criar(String username, String password, List<String> roles);

  record UsuarioResponse(String username, List<String> roles) {}
}

package br.com.condominio.identidade.adapter.in.web;

import br.com.condominio.identidade.adapter.in.web.dto.CriarUsuarioRequest;
import br.com.condominio.identidade.adapter.in.web.dto.UsuarioResponse;
import br.com.condominio.identidade.application.port.in.CriarUsuarioAdminUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

  private final CriarUsuarioAdminUseCase criarUsuarioAdminUseCase;

  public AdminUserController(CriarUsuarioAdminUseCase criarUsuarioAdminUseCase) {
    this.criarUsuarioAdminUseCase = criarUsuarioAdminUseCase;
  }

  @PostMapping
  public ResponseEntity<UsuarioResponse> criar(@RequestBody CriarUsuarioRequest request) {
    CriarUsuarioAdminUseCase.UsuarioResponse response =
        criarUsuarioAdminUseCase.criar(request.username(), request.password(), request.roles());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new UsuarioResponse(response.username(), response.roles()));
  }
}

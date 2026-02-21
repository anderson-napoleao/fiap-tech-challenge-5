package br.com.condominio.identidade.adapter.in.web;

import br.com.condominio.identidade.adapter.in.web.dto.CriarUsuarioRequest;
import br.com.condominio.identidade.adapter.in.web.dto.UsuarioResponse;
import br.com.condominio.identidade.application.port.in.CriarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.in.DesabilitarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.in.RemoverUsuarioAdminUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

  private final CriarUsuarioAdminUseCase criarUsuarioAdminUseCase;
  private final RemoverUsuarioAdminUseCase removerUsuarioAdminUseCase;
  private final DesabilitarUsuarioAdminUseCase desabilitarUsuarioAdminUseCase;

  public AdminUserController(
      CriarUsuarioAdminUseCase criarUsuarioAdminUseCase,
      RemoverUsuarioAdminUseCase removerUsuarioAdminUseCase,
      DesabilitarUsuarioAdminUseCase desabilitarUsuarioAdminUseCase
  ) {
    this.criarUsuarioAdminUseCase = criarUsuarioAdminUseCase;
    this.removerUsuarioAdminUseCase = removerUsuarioAdminUseCase;
    this.desabilitarUsuarioAdminUseCase = desabilitarUsuarioAdminUseCase;
  }

  @PostMapping
  public ResponseEntity<UsuarioResponse> criar(@RequestBody CriarUsuarioRequest request) {
    CriarUsuarioAdminUseCase.UsuarioResponse response = criarUsuarioAdminUseCase.criar(
        new CriarUsuarioAdminUseCase.Command(request.email(), request.password(), request.role())
    );

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new UsuarioResponse(response.id(), response.email()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> remover(@PathVariable("id") String identityId) {
    removerUsuarioAdminUseCase.remover(new RemoverUsuarioAdminUseCase.Command(identityId));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/disable")
  public ResponseEntity<Void> desabilitar(@PathVariable("id") String identityId) {
    desabilitarUsuarioAdminUseCase.desabilitar(new DesabilitarUsuarioAdminUseCase.Command(identityId));
    return ResponseEntity.noContent().build();
  }
}

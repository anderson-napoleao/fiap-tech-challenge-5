package br.com.condominio.identidade.adapter.in.web;

import br.com.condominio.identidade.adapter.in.web.dto.CriarUsuarioRequest;
import br.com.condominio.identidade.adapter.in.web.dto.UsuarioResponse;
import br.com.condominio.identidade.application.port.in.CriarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.in.DesabilitarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.in.RemoverUsuarioAdminUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

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
    log.info("admin user creation requested for email {}", maskEmail(request.email()));
    CriarUsuarioAdminUseCase.UsuarioResponse response = criarUsuarioAdminUseCase.criar(
        new CriarUsuarioAdminUseCase.Command(request.email(), request.password(), request.role())
    );

    log.info("admin user created with identity {}", summarizeIdentity(response.id()));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new UsuarioResponse(response.id(), response.email()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> remover(@PathVariable("id") String identityId) {
    log.info("admin user removal requested for identity {}", summarizeIdentity(identityId));
    removerUsuarioAdminUseCase.remover(new RemoverUsuarioAdminUseCase.Command(identityId));
    log.info("admin user removed for identity {}", summarizeIdentity(identityId));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/disable")
  public ResponseEntity<Void> desabilitar(@PathVariable("id") String identityId) {
    log.info("admin user disable requested for identity {}", summarizeIdentity(identityId));
    desabilitarUsuarioAdminUseCase.desabilitar(new DesabilitarUsuarioAdminUseCase.Command(identityId));
    log.info("admin user disabled for identity {}", summarizeIdentity(identityId));
    return ResponseEntity.noContent().build();
  }

  private String summarizeIdentity(String identityId) {
    if (identityId == null || identityId.isBlank()) {
      return "unknown";
    }
    if (identityId.length() <= 6) {
      return identityId;
    }
    return "***" + identityId.substring(identityId.length() - 6);
  }

  private String maskEmail(String value) {
    if (value == null || value.isBlank()) {
      return "unknown";
    }

    int atIndex = value.indexOf('@');
    if (atIndex <= 0 || atIndex == value.length() - 1) {
      return "***";
    }

    String localPart = value.substring(0, atIndex);
    String domain = value.substring(atIndex + 1);
    String prefix = localPart.length() <= 3 ? localPart.substring(0, 1) : localPart.substring(0, 3);
    return prefix + "***@" + domain;
  }
}

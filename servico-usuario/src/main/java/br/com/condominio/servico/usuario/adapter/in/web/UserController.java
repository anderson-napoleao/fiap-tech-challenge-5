package br.com.condominio.servico.usuario.adapter.in.web;

import br.com.condominio.servico.usuario.adapter.in.web.dto.AtualizarUsuarioRequest;
import br.com.condominio.servico.usuario.adapter.in.web.dto.CriarUsuarioRequest;
import br.com.condominio.servico.usuario.adapter.in.web.dto.UsuarioResponse;
import br.com.condominio.servico.usuario.application.port.in.AtualizarMeuPerfilUseCase;
import br.com.condominio.servico.usuario.application.port.in.CadastrarUsuarioUseCase;
import br.com.condominio.servico.usuario.application.port.in.ObterMeuPerfilUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

  private static final Logger log = LoggerFactory.getLogger(UserController.class);

  private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;
  private final ObterMeuPerfilUseCase obterMeuPerfilUseCase;
  private final AtualizarMeuPerfilUseCase atualizarMeuPerfilUseCase;

  public UserController(
      CadastrarUsuarioUseCase cadastrarUsuarioUseCase,
      ObterMeuPerfilUseCase obterMeuPerfilUseCase,
      AtualizarMeuPerfilUseCase atualizarMeuPerfilUseCase
  ) {
    this.cadastrarUsuarioUseCase = cadastrarUsuarioUseCase;
    this.obterMeuPerfilUseCase = obterMeuPerfilUseCase;
    this.atualizarMeuPerfilUseCase = atualizarMeuPerfilUseCase;
  }

  @PostMapping
  public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CriarUsuarioRequest request) {
    log.info("user registration requested for email {}", maskEmail(request.email()));

    CadastrarUsuarioUseCase.Result result = cadastrarUsuarioUseCase.executar(
        new CadastrarUsuarioUseCase.Command(
            request.nomeCompleto(),
            request.email(),
            request.senha(),
            request.tipo(),
            request.telefone(),
            request.cpf(),
            request.apartamento(),
            request.bloco()
        )
    );

    log.info("user registered with id {} and identity {}", summarizeIdentifier(result.id()), summarizeIdentifier(result.identityId()));
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
  }

  @GetMapping("/me")
  public UsuarioResponse buscarMeuPerfil(@AuthenticationPrincipal Jwt jwt) {
    log.info("profile query requested for subject {}", summarizeIdentifier(jwt.getSubject()));
    ObterMeuPerfilUseCase.Result result = obterMeuPerfilUseCase.executar(
        new ObterMeuPerfilUseCase.Command(jwt.getSubject())
    );
    log.info("profile query completed for user {}", summarizeIdentifier(result.id()));
    return toResponse(result);
  }

  @PutMapping("/me")
  public UsuarioResponse atualizarMeuPerfil(
      @AuthenticationPrincipal Jwt jwt,
      @RequestBody AtualizarUsuarioRequest request
  ) {
    log.info("profile update requested for subject {}", summarizeIdentifier(jwt.getSubject()));
    AtualizarMeuPerfilUseCase.Result result = atualizarMeuPerfilUseCase.executar(
        new AtualizarMeuPerfilUseCase.Command(
            jwt.getSubject(),
            request.nomeCompleto(),
            request.telefone(),
            request.cpf(),
            request.apartamento(),
            request.bloco()
        )
    );

    log.info("profile updated for user {}", summarizeIdentifier(result.id()));
    return toResponse(result);
  }

  private String summarizeIdentifier(Object value) {
    if (value == null) {
      return "unknown";
    }

    String normalized = String.valueOf(value);
    if (normalized.isBlank()) {
      return "unknown";
    }

    if (normalized.length() <= 6) {
      return normalized;
    }
    return "***" + normalized.substring(normalized.length() - 6);
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

  private UsuarioResponse toResponse(CadastrarUsuarioUseCase.Result result) {
    return new UsuarioResponse(
        result.id(),
        result.identityId(),
        result.nomeCompleto(),
        result.email(),
        result.tipo(),
        result.telefone(),
        result.cpf(),
        result.apartamento(),
        result.bloco()
    );
  }

  private UsuarioResponse toResponse(ObterMeuPerfilUseCase.Result result) {
    return new UsuarioResponse(
        result.id(),
        result.identityId(),
        result.nomeCompleto(),
        result.email(),
        result.tipo(),
        result.telefone(),
        result.cpf(),
        result.apartamento(),
        result.bloco()
    );
  }

  private UsuarioResponse toResponse(AtualizarMeuPerfilUseCase.Result result) {
    return new UsuarioResponse(
        result.id(),
        result.identityId(),
        result.nomeCompleto(),
        result.email(),
        result.tipo(),
        result.telefone(),
        result.cpf(),
        result.apartamento(),
        result.bloco()
    );
  }
}

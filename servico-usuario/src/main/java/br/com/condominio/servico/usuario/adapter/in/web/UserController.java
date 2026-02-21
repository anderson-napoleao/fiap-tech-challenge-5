package br.com.condominio.servico.usuario.adapter.in.web;

import br.com.condominio.servico.usuario.adapter.in.web.dto.AtualizarUsuarioRequest;
import br.com.condominio.servico.usuario.adapter.in.web.dto.CriarUsuarioRequest;
import br.com.condominio.servico.usuario.adapter.in.web.dto.UsuarioResponse;
import br.com.condominio.servico.usuario.application.port.in.AtualizarMeuPerfilUseCase;
import br.com.condominio.servico.usuario.application.port.in.CadastrarUsuarioUseCase;
import br.com.condominio.servico.usuario.application.port.in.ObterMeuPerfilUseCase;
import jakarta.validation.Valid;
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

    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
  }

  @GetMapping("/me")
  public UsuarioResponse buscarMeuPerfil(@AuthenticationPrincipal Jwt jwt) {
    ObterMeuPerfilUseCase.Result result = obterMeuPerfilUseCase.executar(
        new ObterMeuPerfilUseCase.Command(jwt.getSubject())
    );
    return toResponse(result);
  }

  @PutMapping("/me")
  public UsuarioResponse atualizarMeuPerfil(
      @AuthenticationPrincipal Jwt jwt,
      @RequestBody AtualizarUsuarioRequest request
  ) {
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

    return toResponse(result);
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

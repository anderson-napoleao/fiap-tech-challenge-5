package br.com.condominio.identidade.adapter.in.web;

import br.com.condominio.identidade.adapter.in.web.dto.AuthTokenRequest;
import br.com.condominio.identidade.adapter.in.web.dto.AuthTokenResponse;
import br.com.condominio.identidade.application.exception.CredenciaisInvalidasException;
import br.com.condominio.identidade.application.port.in.GerarTokenUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final GerarTokenUseCase gerarTokenUseCase;

  public AuthController(GerarTokenUseCase gerarTokenUseCase) {
    this.gerarTokenUseCase = gerarTokenUseCase;
  }

  @PostMapping("/token")
  public ResponseEntity<AuthTokenResponse> token(@RequestBody AuthTokenRequest request) {
    if (request == null) {
      return ResponseEntity.badRequest().build();
    }

    try {
      GerarTokenUseCase.Result result = gerarTokenUseCase.gerar(
          new GerarTokenUseCase.Command(request.username(), request.password())
      );

      return ResponseEntity.ok(new AuthTokenResponse(
          result.accessToken(),
          result.tokenType(),
          result.expiresIn()
      ));
    } catch (CredenciaisInvalidasException exception) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (IllegalArgumentException exception) {
      return ResponseEntity.badRequest().build();
    }
  }
}

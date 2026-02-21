package br.com.condominio.identidade.adapter.in.web;

import br.com.condominio.identidade.adapter.in.web.dto.AuthTokenRequest;
import br.com.condominio.identidade.adapter.in.web.dto.AuthTokenResponse;
import br.com.condominio.identidade.application.exception.CredenciaisInvalidasException;
import br.com.condominio.identidade.application.port.in.GerarTokenUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  private final GerarTokenUseCase gerarTokenUseCase;

  public AuthController(GerarTokenUseCase gerarTokenUseCase) {
    this.gerarTokenUseCase = gerarTokenUseCase;
  }

  @PostMapping("/token")
  public ResponseEntity<AuthTokenResponse> token(@RequestBody AuthTokenRequest request) {
    if (request == null) {
      log.warn("token request rejected: request body is null");
      return ResponseEntity.badRequest().build();
    }

    String maskedUsername = maskEmail(request.username());
    log.info("token request received for user {}", maskedUsername);

    try {
      GerarTokenUseCase.Result result = gerarTokenUseCase.gerar(
          new GerarTokenUseCase.Command(request.username(), request.password())
      );

      log.info("token generated successfully for user {}", maskedUsername);
      return ResponseEntity.ok(new AuthTokenResponse(
          result.accessToken(),
          result.tokenType(),
          result.expiresIn()
      ));
    } catch (CredenciaisInvalidasException exception) {
      log.warn("token request denied for user {}", maskedUsername);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (IllegalArgumentException exception) {
      log.warn("token request invalid for user {}", maskedUsername, exception);
      return ResponseEntity.badRequest().build();
    }
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

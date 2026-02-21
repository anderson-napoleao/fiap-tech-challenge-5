package br.com.condominio.identidade.infrastructure.security;

import br.com.condominio.identidade.application.port.out.TokenJwtPort;
import br.com.condominio.identidade.application.port.out.UsuarioStorePort;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenServiceAdapter implements TokenJwtPort {

  private final JwtEncoder jwtEncoder;
  private final String issuer;
  private final long expiresInSeconds;

  public JwtTokenServiceAdapter(
      JwtEncoder jwtEncoder,
      @Value("${security.jwt.issuer:servico-identidade}") String issuer,
      @Value("${security.jwt.expires-seconds:3600}") long expiresInSeconds
  ) {
    this.jwtEncoder = jwtEncoder;
    this.issuer = issuer;
    this.expiresInSeconds = expiresInSeconds;
  }

  @Override
  public TokenGerado gerarToken(UsuarioStorePort.IdentityUserData usuario) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(expiresInSeconds);

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(issuer)
        .subject(usuario.id())
        .issuedAt(now)
        .expiresAt(expiresAt)
        .claim("email", usuario.email())
        .claim("roles", usuario.roles())
        .build();

    String accessToken = jwtEncoder.encode(
        JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
    ).getTokenValue();

    return new TokenGerado(accessToken, "Bearer", expiresInSeconds);
  }
}

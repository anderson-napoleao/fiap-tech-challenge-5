package br.com.condominio.servico.usuario.adapter.out;

import br.com.condominio.servico.usuario.application.port.out.IdentityGatewayPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IdentityGatewayAdapter implements IdentityGatewayPort {

  private final RestClient restClient;
  private final String identityBaseUrl;
  private final String identityAdminUsername;
  private final String identityAdminPassword;

  public IdentityGatewayAdapter(
      RestClient.Builder restClientBuilder,
      @Value("${identity.base-url:http://localhost:8081}") String identityBaseUrl,
      @Value("${identity.admin.username:admin}") String identityAdminUsername,
      @Value("${identity.admin.password:admin}") String identityAdminPassword
  ) {
    this.restClient = restClientBuilder.build();
    this.identityBaseUrl = identityBaseUrl;
    this.identityAdminUsername = identityAdminUsername;
    this.identityAdminPassword = identityAdminPassword;
  }

  @Override
  public CriarIdentidadeResult criarUsuario(CriarIdentidadeCommand command) {
    CreateIdentityResponse response = restClient
        .post()
        .uri(identityBaseUrl + "/admin/users")
        .headers(this::applyAdminBasicAuth)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new CreateIdentityRequest(command.email(), command.senha(), command.role()))
        .retrieve()
        .body(CreateIdentityResponse.class);

    if (response == null || response.id() == null) {
      throw new IllegalStateException("Falha ao criar usuario no servico-identidade");
    }

    return new CriarIdentidadeResult(response.id(), response.email());
  }

  @Override
  public void removerUsuario(String identityId) {
    restClient
        .delete()
        .uri(identityBaseUrl + "/admin/users/{id}", identityId)
        .headers(this::applyAdminBasicAuth)
        .retrieve()
        .toBodilessEntity();
  }

  @Override
  public void desabilitarUsuario(String identityId) {
    restClient
        .patch()
        .uri(identityBaseUrl + "/admin/users/{id}/disable", identityId)
        .headers(this::applyAdminBasicAuth)
        .retrieve()
        .toBodilessEntity();
  }

  private void applyAdminBasicAuth(HttpHeaders headers) {
    headers.setBasicAuth(identityAdminUsername, identityAdminPassword);
  }

  private record CreateIdentityRequest(String email, String password, String role) {
  }

  private record CreateIdentityResponse(String id, String email) {
  }
}

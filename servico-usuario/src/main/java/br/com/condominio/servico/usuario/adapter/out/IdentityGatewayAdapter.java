package br.com.condominio.servico.usuario.adapter.out;

import br.com.condominio.servico.usuario.application.port.out.IdentityGatewayPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IdentityGatewayAdapter implements IdentityGatewayPort {

  private final RestClient restClient;
  private final String identityBaseUrl;

  public IdentityGatewayAdapter(
      RestClient.Builder restClientBuilder,
      @Value("${identity.base-url:http://localhost:8081}") String identityBaseUrl
  ) {
    this.restClient = restClientBuilder.build();
    this.identityBaseUrl = identityBaseUrl;
  }

  @Override
  public CriarIdentidadeResult criarUsuario(CriarIdentidadeCommand command) {
    CreateIdentityResponse response = restClient
        .post()
        .uri(identityBaseUrl + "/admin/users")
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
        .retrieve()
        .toBodilessEntity();
  }

  @Override
  public void desabilitarUsuario(String identityId) {
    restClient
        .patch()
        .uri(identityBaseUrl + "/admin/users/{id}/disable", identityId)
        .retrieve()
        .toBodilessEntity();
  }

  private record CreateIdentityRequest(String email, String password, String role) {
  }

  private record CreateIdentityResponse(String id, String email) {
  }
}

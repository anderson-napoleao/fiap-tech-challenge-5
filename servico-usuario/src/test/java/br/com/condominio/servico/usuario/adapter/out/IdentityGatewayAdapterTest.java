package br.com.condominio.servico.usuario.adapter.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import br.com.condominio.servico.usuario.application.port.out.IdentityGatewayPort;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class IdentityGatewayAdapterTest {

  @Mock
  private Tracer tracer;

  private MockRestServiceServer server;
  private IdentityGatewayAdapter adapter;

  @BeforeEach
  void setup() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    adapter = new IdentityGatewayAdapter(
        builder,
        tracer,
        "http://localhost:8081",
        "admin",
        "admin"
    );
    when(tracer.currentSpan()).thenReturn(null);
  }

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  @Test
  void deveCriarUsuarioNoServicoDeIdentidade() {
    MDC.put("traceId", "0123456789abcdef0123456789abcdef");
    MDC.put("spanId", "0123456789abcdef");

    server.expect(requestTo("http://localhost:8081/admin/users"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic YWRtaW46YWRtaW4="))
        .andExpect(header("traceparent", "00-0123456789abcdef0123456789abcdef-0123456789abcdef-01"))
        .andRespond(withSuccess("{\"id\":\"id-1\",\"email\":\"maria@teste.com\"}", MediaType.APPLICATION_JSON));

    IdentityGatewayPort.CriarIdentidadeResult result = adapter.criarUsuario(
        new IdentityGatewayPort.CriarIdentidadeCommand("maria@teste.com", "123456", "ROLE_MORADOR")
    );

    assertEquals("id-1", result.identityId());
    assertEquals("maria@teste.com", result.email());
    server.verify();
  }

  @Test
  void deveFalharQuandoRespostaNaoPossuiId() {
    server.expect(requestTo("http://localhost:8081/admin/users"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"email\":\"maria@teste.com\"}", MediaType.APPLICATION_JSON));

    assertThrows(
        IllegalStateException.class,
        () -> adapter.criarUsuario(
            new IdentityGatewayPort.CriarIdentidadeCommand("maria@teste.com", "123456", "ROLE_MORADOR")
        )
    );
    server.verify();
  }

  @Test
  void deveRemoverUsuarioNoServicoDeIdentidade() {
    server.expect(requestTo("http://localhost:8081/admin/users/id-77"))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withStatus(HttpStatus.NO_CONTENT));

    adapter.removerUsuario("id-77");
    server.verify();
  }

  @Test
  void devePropagarErroAoDesabilitarUsuario() {
    server.expect(requestTo("http://localhost:8081/admin/users/id-55/disable"))
        .andExpect(method(HttpMethod.PATCH))
        .andRespond(withServerError());

    assertThrows(RuntimeException.class, () -> adapter.desabilitarUsuario("id-55"));
    server.verify();
  }
}


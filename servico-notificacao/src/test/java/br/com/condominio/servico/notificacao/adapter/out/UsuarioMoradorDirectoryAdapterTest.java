package br.com.condominio.servico.notificacao.adapter.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import br.com.condominio.servico.notificacao.application.port.out.MoradorDirectoryPort;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class UsuarioMoradorDirectoryAdapterTest {

  @Mock
  private Tracer tracer;

  @Mock
  private Span span;

  @Mock
  private TraceContext traceContext;

  private MockRestServiceServer server;
  private UsuarioMoradorDirectoryAdapter adapter;

  @BeforeEach
  void setup() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    adapter = new UsuarioMoradorDirectoryAdapter(
        builder,
        tracer,
        "http://localhost:8082",
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
  void deveListarMoradoresDaUnidadeComTokenDeServico() {
    MDC.put("traceId", "0123456789abcdef0123456789abcdef");
    MDC.put("spanId", "0123456789abcdef");

    server.expect(requestTo("http://localhost:8081/auth/token"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("traceparent", "00-0123456789abcdef0123456789abcdef-0123456789abcdef-01"))
        .andRespond(withSuccess("{\"access_token\":\"token-1\",\"token_type\":\"Bearer\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));

    server.expect(requestTo("http://localhost:8082/interno/usuarios/moradores?bloco=A&apartamento=101"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer token-1"))
        .andExpect(header("traceparent", "00-0123456789abcdef0123456789abcdef-0123456789abcdef-01"))
        .andRespond(withSuccess(
            "[{\"identityId\":\"id-1\",\"nomeCompleto\":\"Maria\",\"email\":\"maria@local\"}]",
            MediaType.APPLICATION_JSON
        ));

    List<MoradorDirectoryPort.Morador> moradores = adapter.listarMoradoresPorUnidade("A", "101");

    assertEquals(1, moradores.size());
    assertEquals("id-1", moradores.getFirst().identityId());
    assertEquals("Maria", moradores.getFirst().nomeCompleto());
    assertEquals("maria@local", moradores.getFirst().email());
    server.verify();
  }

  @Test
  void deveRetornarListaVaziaQuandoNaoExistirMoradorNaUnidade() {
    server.expect(requestTo("http://localhost:8081/auth/token"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"access_token\":\"token-1\",\"token_type\":\"Bearer\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));

    server.expect(requestTo("http://localhost:8082/interno/usuarios/moradores?bloco=B&apartamento=202"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer token-1"))
        .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

    List<MoradorDirectoryPort.Morador> moradores = adapter.listarMoradoresPorUnidade("B", "202");

    assertEquals(0, moradores.size());
    server.verify();
  }

  @Test
  void deveGerarTraceparentUsandoTracerQuandoNaoHouverMdc() {
    when(tracer.currentSpan()).thenReturn(span);
    when(span.context()).thenReturn(traceContext);
    when(traceContext.traceId()).thenReturn("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    when(traceContext.spanId()).thenReturn("bbbbbbbbbbbbbbbb");

    server.expect(requestTo("http://localhost:8081/auth/token"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("traceparent", "00-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa-bbbbbbbbbbbbbbbb-01"))
        .andRespond(withSuccess("{\"access_token\":\"token-1\",\"token_type\":\"Bearer\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));

    server.expect(requestTo("http://localhost:8082/interno/usuarios/moradores?bloco=C&apartamento=303"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer token-1"))
        .andExpect(header("traceparent", "00-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa-bbbbbbbbbbbbbbbb-01"))
        .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

    List<MoradorDirectoryPort.Morador> moradores = adapter.listarMoradoresPorUnidade("C", "303");

    assertEquals(0, moradores.size());
    server.verify();
  }

  @Test
  void deveFalharQuandoTokenDeServicoNaoForRetornado() {
    server.expect(requestTo("http://localhost:8081/auth/token"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"token_type\":\"Bearer\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));

    assertThrows(IllegalStateException.class, () -> adapter.listarMoradoresPorUnidade("A", "101"));
    server.verify();
  }

  @Test
  void devePropagarErroQuandoServicoUsuarioFalhar() {
    server.expect(requestTo("http://localhost:8081/auth/token"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"access_token\":\"token-1\",\"token_type\":\"Bearer\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));

    server.expect(requestTo("http://localhost:8082/interno/usuarios/moradores?bloco=A&apartamento=101"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withServerError());

    assertThrows(RuntimeException.class, () -> adapter.listarMoradoresPorUnidade("A", "101"));
    server.verify();
  }
}

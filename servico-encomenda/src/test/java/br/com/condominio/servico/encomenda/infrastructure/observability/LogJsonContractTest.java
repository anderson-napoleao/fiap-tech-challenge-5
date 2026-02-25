package br.com.condominio.servico.encomenda.infrastructure.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.condominio.servico.encomenda.adapter.in.web.PortariaEncomendaController;
import br.com.condominio.servico.encomenda.application.exception.EncomendaNaoEncontradaException;
import br.com.condominio.servico.encomenda.application.port.in.BaixarEncomendaRetiradaUseCase;
import br.com.condominio.servico.encomenda.application.port.in.ReceberEncomendaUseCase;
import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import br.com.condominio.servico.encomenda.infrastructure.security.SecurityConfig;
import br.com.condominio.servico.encomenda.infrastructure.web.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PortariaEncomendaController.class)
@Import({
    SecurityConfig.class,
    GlobalExceptionHandler.class,
    HttpLoggingInterceptor.class,
    ObservabilityWebMvcConfig.class
})
@TestPropertySource(properties = {
    "security.jwt.secret=condominio-jwt-secret-local-2026-seguro",
    "security.jwt.issuer=servico-identidade"
})
@ExtendWith(OutputCaptureExtension.class)
class LogJsonContractTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ReceberEncomendaUseCase receberEncomendaUseCase;

  @MockBean
  private BaixarEncomendaRetiradaUseCase baixarEncomendaRetiradaUseCase;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void deveGerarLogsJsonComCamposObrigatoriosEmSucessoEErro(CapturedOutput output) throws Exception {
    when(receberEncomendaUseCase.executar(any()))
        .thenReturn(new ReceberEncomendaUseCase.Result(
            1L,
            "Maria",
            "101",
            "A",
            "Caixa pequena",
            "porteiro-1",
            StatusEncomenda.RECEBIDA,
            Instant.parse("2026-02-25T00:00:00Z")
        ));
    when(baixarEncomendaRetiradaUseCase.executar(any()))
        .thenThrow(new EncomendaNaoEncontradaException("Encomenda nao encontrada"));

    mockMvc.perform(post("/portaria/encomendas")
            .with(jwt()
                .jwt(jwt -> jwt.subject("porteiro-1").claim("roles", List.of("ROLE_FUNCIONARIO")))
                .authorities(new SimpleGrantedAuthority("ROLE_FUNCIONARIO")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "nomeDestinatario": "Maria",
                  "apartamento": "101",
                  "bloco": "A",
                  "descricao": "Caixa pequena"
                }
                """))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/portaria/encomendas/999/retirada")
            .with(jwt()
                .jwt(jwt -> jwt.subject("porteiro-1").claim("roles", List.of("ROLE_FUNCIONARIO")))
                .authorities(new SimpleGrantedAuthority("ROLE_FUNCIONARIO")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "retiradoPorNome": "Maria"
                }
                """))
        .andExpect(status().isNotFound());

    List<JsonNode> logs = parseJsonLogs(output.getOut(), "servico-encomenda");
    JsonNode successLog = findRequestLog(logs, "/portaria/encomendas", "201");
    JsonNode errorLog = findRequestLog(logs, "/portaria/encomendas/999/retirada", "404");
    JsonNode businessErrorLog = findErrorLog(logs, "EncomendaNaoEncontradaException");

    assertMandatoryFields(successLog);
    assertMandatoryFields(errorLog);
    assertMandatoryFields(businessErrorLog);
  }

  private List<JsonNode> parseJsonLogs(String rawOutput, String serviceName) throws Exception {
    List<JsonNode> result = new ArrayList<>();
    for (String line : rawOutput.split("\\R")) {
      String candidate = line.trim();
      if (!candidate.startsWith("{") || !candidate.endsWith("}")) {
        continue;
      }

      JsonNode node = objectMapper.readTree(candidate);
      if (!serviceName.equals(node.path("service").asText())) {
        continue;
      }
      result.add(node);
    }
    return result;
  }

  private JsonNode findRequestLog(List<JsonNode> logs, String path, String statusCode) {
    return logs.stream()
        .filter(node -> "request_in".equals(node.path("event_type").asText()))
        .filter(node -> path.equals(node.path("http.path").asText()))
        .filter(node -> statusCode.equals(node.path("http.status_code").asText()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "Nao encontrou log request_in para path=" + path + " e status=" + statusCode));
  }

  private JsonNode findErrorLog(List<JsonNode> logs, String errorType) {
    return logs.stream()
        .filter(node -> "error".equals(node.path("event_type").asText()))
        .filter(node -> errorType.equals(node.path("error.type").asText()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Nao encontrou log de erro com error.type=" + errorType));
  }

  private void assertMandatoryFields(JsonNode node) {
    assertThat(node.path("@timestamp").asText()).isNotBlank();
    assertThat(node.path("level").asText()).isNotBlank();
    assertThat(node.path("service").asText()).isEqualTo("servico-encomenda");
    assertThat(node.path("env").asText()).isNotBlank();
    assertThat(node.path("trace_id").asText()).isNotBlank();
    assertThat(node.path("span_id").asText()).isNotBlank();
    assertThat(node.path("logger").asText()).isNotBlank();
    assertThat(node.path("message").asText()).isNotBlank();
  }
}

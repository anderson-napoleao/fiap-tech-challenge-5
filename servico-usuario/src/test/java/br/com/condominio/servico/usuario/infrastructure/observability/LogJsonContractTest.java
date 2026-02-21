package br.com.condominio.servico.usuario.infrastructure.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.condominio.servico.usuario.adapter.in.web.UserController;
import br.com.condominio.servico.usuario.application.exception.NotFoundException;
import br.com.condominio.servico.usuario.application.port.in.AtualizarMeuPerfilUseCase;
import br.com.condominio.servico.usuario.application.port.in.CadastrarUsuarioUseCase;
import br.com.condominio.servico.usuario.application.port.in.ObterMeuPerfilUseCase;
import br.com.condominio.servico.usuario.domain.TipoUsuario;
import br.com.condominio.servico.usuario.infrastructure.security.SecurityConfig;
import br.com.condominio.servico.usuario.infrastructure.web.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(UserController.class)
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
  private CadastrarUsuarioUseCase cadastrarUsuarioUseCase;

  @MockBean
  private ObterMeuPerfilUseCase obterMeuPerfilUseCase;

  @MockBean
  private AtualizarMeuPerfilUseCase atualizarMeuPerfilUseCase;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void deveGerarLogsJsonComCamposObrigatoriosEmSucessoEErro(CapturedOutput output) throws Exception {
    when(cadastrarUsuarioUseCase.executar(any()))
        .thenReturn(new CadastrarUsuarioUseCase.Result(
            1L,
            "id-123",
            "Maria",
            "maria@teste.com",
            TipoUsuario.MORADOR,
            null,
            null,
            "101",
            "A"
        ));
    when(obterMeuPerfilUseCase.executar(any()))
        .thenThrow(new NotFoundException("Usuario nao encontrado"));

    mockMvc
        .perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "nomeCompleto": "Maria",
                  "email": "maria@teste.com",
                  "senha": "123456",
                  "tipo": "MORADOR",
                  "apartamento": "101",
                  "bloco": "A"
                }
                """))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/users/me").with(jwt().jwt(jwt -> jwt.subject("id-404"))))
        .andExpect(status().isNotFound());

    List<JsonNode> logs = parseJsonLogs(output.getOut(), "servico-usuario");
    JsonNode successLog = findRequestLog(logs, "/users", "201");
    JsonNode errorLog = findRequestLog(logs, "/users/me", "404");
    JsonNode businessErrorLog = findErrorLog(logs, "NotFoundException");

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
    assertThat(node.path("service").asText()).isEqualTo("servico-usuario");
    assertThat(node.path("env").asText()).isNotBlank();
    assertThat(node.path("trace_id").asText()).isNotBlank();
    assertThat(node.path("span_id").asText()).isNotBlank();
    assertThat(node.path("logger").asText()).isNotBlank();
    assertThat(node.path("message").asText()).isNotBlank();
  }
}

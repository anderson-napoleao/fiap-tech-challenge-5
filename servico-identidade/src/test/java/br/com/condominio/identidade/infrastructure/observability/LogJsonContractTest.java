package br.com.condominio.identidade.infrastructure.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class LogJsonContractTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void deveGerarLogsJsonComCamposObrigatoriosEmSucessoEErro(CapturedOutput output) throws Exception {
    String email = "json.log." + System.nanoTime() + "@teste.com";

    mockMvc
        .perform(post("/admin/users")
            .header("Authorization", basicAuth("admin", "admin"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "%s",
                  "password": "123456",
                  "role": "USER"
                }
                """.formatted(email)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(post("/auth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "username": "%s",
                  "password": "123456"
                }
                """.formatted(email)))
        .andExpect(status().isOk());

    mockMvc
        .perform(post("/auth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "username": "",
                  "password": ""
                }
                """))
        .andExpect(status().isBadRequest());

    List<JsonNode> logs = parseJsonLogs(output.getOut(), "servico-identidade");
    JsonNode successLog = findRequestLog(logs, "/auth/token", "200");
    JsonNode errorLog = findRequestLog(logs, "/auth/token", "400");

    assertMandatoryFields(successLog);
    assertMandatoryFields(errorLog);
  }

  private static String basicAuth(String username, String password) {
    String credentials = username + ":" + password;
    String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    return "Basic " + encoded;
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

  private void assertMandatoryFields(JsonNode node) {
    assertThat(node.path("@timestamp").asText()).isNotBlank();
    assertThat(node.path("level").asText()).isNotBlank();
    assertThat(node.path("service").asText()).isEqualTo("servico-identidade");
    assertThat(node.path("env").asText()).isNotBlank();
    assertThat(node.path("trace_id").asText()).isNotBlank();
    assertThat(node.path("span_id").asText()).isNotBlank();
    assertThat(node.path("logger").asText()).isNotBlank();
    assertThat(node.path("message").asText()).isNotBlank();
  }
}

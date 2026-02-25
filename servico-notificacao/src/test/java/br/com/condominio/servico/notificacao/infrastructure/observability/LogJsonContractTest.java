package br.com.condominio.servico.notificacao.infrastructure.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.condominio.servico.notificacao.adapter.in.web.MoradorNotificacaoController;
import br.com.condominio.servico.notificacao.application.exception.NotificacaoNaoEncontradaException;
import br.com.condominio.servico.notificacao.application.port.in.ConfirmarRecebimentoNotificacaoUseCase;
import br.com.condominio.servico.notificacao.application.port.in.ListarNotificacoesPendentesUseCase;
import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import br.com.condominio.servico.notificacao.infrastructure.security.SecurityConfig;
import br.com.condominio.servico.notificacao.infrastructure.web.error.GlobalExceptionHandler;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MoradorNotificacaoController.class)
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
  private ListarNotificacoesPendentesUseCase listarNotificacoesPendentesUseCase;

  @MockBean
  private ConfirmarRecebimentoNotificacaoUseCase confirmarRecebimentoNotificacaoUseCase;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void deveGerarLogsJsonComCamposObrigatoriosEmSucessoEErro(CapturedOutput output) throws Exception {
    when(listarNotificacoesPendentesUseCase.executar(any()))
        .thenReturn(new ListarNotificacoesPendentesUseCase.Result(
            List.of(
                new ListarNotificacoesPendentesUseCase.Item(
                    "not-1",
                    "enc-1",
                    CanalNotificacao.PUSH,
                    "device-1",
                    "Sua encomenda chegou",
                    StatusNotificacao.ENVIADA,
                    Instant.parse("2026-02-25T00:00:00Z"),
                    Instant.parse("2026-02-25T00:01:00Z")
                )
            ),
            0,
            10
        ));
    when(confirmarRecebimentoNotificacaoUseCase.executar(any()))
        .thenThrow(new NotificacaoNaoEncontradaException("Notificacao nao encontrada"));

    mockMvc.perform(
            get("/morador/notificacoes")
                .param("confirmada", "false")
                .param("page", "0")
                .param("size", "10")
                .with(jwt()
                    .jwt(jwt -> jwt.subject("morador-1"))
                    .authorities(new SimpleGrantedAuthority("ROLE_MORADOR")))
        )
        .andExpect(status().isOk());

    mockMvc.perform(
            post("/morador/notificacoes/not-404/confirmacao")
                .with(jwt()
                    .jwt(jwt -> jwt.subject("morador-1"))
                    .authorities(new SimpleGrantedAuthority("ROLE_MORADOR")))
        )
        .andExpect(status().isNotFound());

    List<JsonNode> logs = parseJsonLogs(output.getOut(), "servico-notificacao");
    JsonNode successLog = findRequestLog(logs, "/morador/notificacoes", "200");
    JsonNode errorLog = findRequestLog(logs, "/morador/notificacoes/not-404/confirmacao", "404");
    JsonNode businessErrorLog = findErrorLog(logs, "NotificacaoNaoEncontradaException");

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
    assertThat(node.path("service").asText()).isEqualTo("servico-notificacao");
    assertThat(node.path("env").asText()).isNotBlank();
    assertThat(node.path("trace_id").asText()).isNotBlank();
    assertThat(node.path("span_id").asText()).isNotBlank();
    assertThat(node.path("logger").asText()).isNotBlank();
    assertThat(node.path("message").asText()).isNotBlank();
  }
}

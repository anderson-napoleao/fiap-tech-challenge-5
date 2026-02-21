package br.com.condominio.identidade.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private static String basicAuth(String username, String password) {
    String credentials = username + ":" + password;
    String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    return "Basic " + encoded;
  }

  @Test
  void deveGerarTokenComCredenciaisValidas() throws Exception {
    String email = "maria+" + System.nanoTime() + "@teste.com";

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
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").isNotEmpty())
        .andExpect(jsonPath("$.token_type").value("Bearer"))
        .andExpect(jsonPath("$.expires_in").isNumber());
  }

  @Test
  void deveRetornar401ComCredenciaisInvalidas() throws Exception {
    mockMvc
        .perform(post("/auth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "username": "nao.existe@teste.com",
                  "password": "senha-incorreta"
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRetornar400QuandoPayloadInvalido() throws Exception {
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
  }
}

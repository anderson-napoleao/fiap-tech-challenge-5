package br.com.condominio.identidade.infrastructure.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OidcEndpointsTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void deveExporOpenIdDiscovery() throws Exception {
    mockMvc
        .perform(get("/.well-known/openid-configuration"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.issuer").exists())
        .andExpect(jsonPath("$.jwks_uri").exists());
  }

  @Test
  void deveExporJwks() throws Exception {
    mockMvc
        .perform(get("/oauth2/jwks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keys").isArray());
  }
}

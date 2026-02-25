package br.com.condominio.servico.notificacao.infrastructure.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfigTest.TestController.class)
class SecurityConfigTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void devePermitirHealthSemAutenticacao() throws Exception {
    mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk());
  }

  @Test
  void deveRetornar401EmEndpointMoradorSemToken() throws Exception {
    mockMvc.perform(get("/morador/ping"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRetornar403ParaTokenSemRoleMorador() throws Exception {
    mockMvc.perform(
            get("/morador/ping")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_FUNCIONARIO")))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void devePermitirEndpointMoradorComRoleMorador() throws Exception {
    mockMvc.perform(
            get("/morador/ping")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MORADOR")))
        )
        .andExpect(status().isOk());
  }

  @RestController
  static class TestController {

    @GetMapping("/morador/ping")
    String pingMorador() {
      return "ok";
    }
  }
}

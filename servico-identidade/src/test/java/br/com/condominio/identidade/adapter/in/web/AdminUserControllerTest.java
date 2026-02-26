package br.com.condominio.identidade.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class AdminUserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private static String basicAuth(String username, String password) {
    String credentials = username + ":" + password;
    String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    return "Basic " + encoded;
  }

  @Test
  void deveCriarUsuarioComContratoEsperadoPeloGateway() throws Exception {
    mockMvc
        .perform(post("/admin/users")
            .header("Authorization", basicAuth("admin", "admin"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "maria@teste.com",
                  "password": "123456",
                  "role": "USER"
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.email").value("maria@teste.com"));
  }

  @Test
  void deveRemoverEDesabilitarSemFalharQuandoIdNaoExiste() throws Exception {
    mockMvc
        .perform(delete("/admin/users/id-inexistente")
            .header("Authorization", basicAuth("admin", "admin")))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(patch("/admin/users/id-inexistente/disable")
            .header("Authorization", basicAuth("admin", "admin")))
        .andExpect(status().isNoContent());
  }

  @Test
  void deveNegarAcessoAdminParaUsuarioAutenticadoSemRoleAdmin() throws Exception {
    String emailUsuarioComum = "usuario.comum." + System.nanoTime() + "@teste.com";

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
                """.formatted(emailUsuarioComum)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(delete("/admin/users/id-qualquer")
            .header("Authorization", basicAuth(emailUsuarioComum, "123456")))
        .andExpect(status().isForbidden());
  }
}

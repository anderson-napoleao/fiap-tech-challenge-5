package br.com.condominio.servico.usuario.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.condominio.servico.usuario.application.port.in.ListarMoradoresPorUnidadeUseCase;
import br.com.condominio.servico.usuario.infrastructure.security.SecurityConfig;
import br.com.condominio.servico.usuario.infrastructure.web.error.GlobalExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternoUsuarioController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
    "security.jwt.secret=condominio-jwt-secret-local-2026-seguro",
    "security.jwt.issuer=servico-identidade"
})
class InternoUsuarioControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ListarMoradoresPorUnidadeUseCase listarMoradoresPorUnidadeUseCase;

  @Test
  void deveRetornar401QuandoNaoAutenticado() throws Exception {
    mockMvc.perform(get("/interno/usuarios/moradores")
            .param("bloco", "A")
            .param("apartamento", "101"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRetornar403QuandoNaoForAdmin() throws Exception {
    mockMvc.perform(get("/interno/usuarios/moradores")
            .param("bloco", "A")
            .param("apartamento", "101")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MORADOR"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void deveListarMoradoresDaUnidadeParaAdmin() throws Exception {
    when(listarMoradoresPorUnidadeUseCase.executar(any()))
        .thenReturn(new ListarMoradoresPorUnidadeUseCase.Result(
            List.of(
                new ListarMoradoresPorUnidadeUseCase.Item(
                    "id-1",
                    "Maria Silva",
                    "maria@condominio.local"
                ),
                new ListarMoradoresPorUnidadeUseCase.Item(
                    "id-2",
                    "Joao Souza",
                    "joao@condominio.local"
                )
            )
        ));

    mockMvc.perform(get("/interno/usuarios/moradores")
            .param("bloco", "A")
            .param("apartamento", "101")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].identityId").value("id-1"))
        .andExpect(jsonPath("$[1].identityId").value("id-2"));
  }
}

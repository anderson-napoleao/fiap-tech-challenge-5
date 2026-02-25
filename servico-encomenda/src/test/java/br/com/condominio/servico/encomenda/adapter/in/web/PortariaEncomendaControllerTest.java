package br.com.condominio.servico.encomenda.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.condominio.servico.encomenda.application.exception.EncomendaNaoEncontradaException;
import br.com.condominio.servico.encomenda.application.port.in.BaixarEncomendaRetiradaUseCase;
import br.com.condominio.servico.encomenda.application.port.in.BuscarEncomendaPorIdUseCase;
import br.com.condominio.servico.encomenda.application.port.in.ReceberEncomendaUseCase;
import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import br.com.condominio.servico.encomenda.infrastructure.security.SecurityConfig;
import br.com.condominio.servico.encomenda.infrastructure.web.error.GlobalExceptionHandler;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PortariaEncomendaController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
    "security.jwt.secret=condominio-jwt-secret-local-2026-seguro",
    "security.jwt.issuer=servico-identidade"
})
class PortariaEncomendaControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ReceberEncomendaUseCase receberEncomendaUseCase;

  @MockBean
  private BaixarEncomendaRetiradaUseCase baixarEncomendaRetiradaUseCase;

  @MockBean
  private BuscarEncomendaPorIdUseCase buscarEncomendaPorIdUseCase;

  @Test
  void deveRetornar401SemToken() throws Exception {
    mockMvc.perform(post("/portaria/encomendas")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "nomeDestinatario": "Maria",
                  "apartamento": "101",
                  "bloco": "A",
                  "descricao": "Caixa pequena"
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRetornar401SemTokenNaBaixa() throws Exception {
    mockMvc.perform(post("/portaria/encomendas/1/retirada")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "retiradoPorNome": "Maria"
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRetornar403SemRoleFuncionario() throws Exception {
    mockMvc.perform(post("/portaria/encomendas")
            .with(jwt().jwt(jwt -> jwt.subject("porteiro-1").claim("roles", List.of("ROLE_USER"))))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "nomeDestinatario": "Maria",
                  "apartamento": "101",
                  "bloco": "A",
                  "descricao": "Caixa pequena"
                }
                """))
        .andExpect(status().isForbidden());
  }

  @Test
  void deveRetornar403SemRoleFuncionarioNaBaixa() throws Exception {
    mockMvc.perform(post("/portaria/encomendas/1/retirada")
            .with(jwt().jwt(jwt -> jwt.subject("porteiro-1").claim("roles", List.of("ROLE_USER"))))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "retiradoPorNome": "Maria"
                }
                """))
        .andExpect(status().isForbidden());
  }

  @Test
  void deveReceberEncomendaComRoleFuncionario() throws Exception {
    when(receberEncomendaUseCase.executar(any()))
        .thenReturn(new ReceberEncomendaUseCase.Result(
            1L,
            "Maria",
            "101",
            "A",
            "Caixa pequena",
            "porteiro-1",
            StatusEncomenda.RECEBIDA,
            Instant.parse("2026-02-21T18:00:00Z")
        ));

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
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.status").value("RECEBIDA"))
        .andExpect(jsonPath("$.recebidoPor").value("porteiro-1"));
  }

  @Test
  void deveRetornar404QuandoEncomendaNaoEncontradaNaBaixa() throws Exception {
    when(baixarEncomendaRetiradaUseCase.executar(any()))
        .thenThrow(new EncomendaNaoEncontradaException("Encomenda nao encontrada"));

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
  }

  @Test
  void deveRetornar409QuandoEncomendaJaEstiverRetirada() throws Exception {
    when(baixarEncomendaRetiradaUseCase.executar(any()))
        .thenThrow(new IllegalStateException("Somente encomenda RECEBIDA pode ser retirada"));

    mockMvc.perform(post("/portaria/encomendas/1/retirada")
            .with(jwt()
                .jwt(jwt -> jwt.subject("porteiro-1").claim("roles", List.of("ROLE_FUNCIONARIO")))
                .authorities(new SimpleGrantedAuthority("ROLE_FUNCIONARIO")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "retiradoPorNome": "Maria"
                }
                """))
        .andExpect(status().isConflict());
  }

  @Test
  void deveBaixarEncomendaComRoleFuncionario() throws Exception {
    when(baixarEncomendaRetiradaUseCase.executar(any()))
        .thenReturn(new BaixarEncomendaRetiradaUseCase.Result(
            1L,
            StatusEncomenda.RETIRADA,
            Instant.parse("2026-02-25T18:10:00Z"),
            "Maria"
        ));

    mockMvc.perform(post("/portaria/encomendas/1/retirada")
            .with(jwt()
                .jwt(jwt -> jwt.subject("porteiro-1").claim("roles", List.of("ROLE_FUNCIONARIO")))
                .authorities(new SimpleGrantedAuthority("ROLE_FUNCIONARIO")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "retiradoPorNome": "Maria"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.status").value("RETIRADA"))
        .andExpect(jsonPath("$.retiradoPorNome").value("Maria"));
  }
}

package br.com.condominio.servico.usuario.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.condominio.servico.usuario.application.exception.ConflictException;
import br.com.condominio.servico.usuario.application.exception.NotFoundException;
import br.com.condominio.servico.usuario.application.port.in.AtualizarMeuPerfilUseCase;
import br.com.condominio.servico.usuario.application.port.in.CadastrarUsuarioUseCase;
import br.com.condominio.servico.usuario.application.port.in.ObterMeuPerfilUseCase;
import br.com.condominio.servico.usuario.domain.TipoUsuario;
import br.com.condominio.servico.usuario.infrastructure.security.SecurityConfig;
import br.com.condominio.servico.usuario.infrastructure.web.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8081/oauth2/jwks")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CadastrarUsuarioUseCase cadastrarUsuarioUseCase;

  @MockBean
  private ObterMeuPerfilUseCase obterMeuPerfilUseCase;

  @MockBean
  private AtualizarMeuPerfilUseCase atualizarMeuPerfilUseCase;

  @Test
  void deveCriarUsuarioSemAutenticacao() throws Exception {
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
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.identityId").value("id-123"));
  }

  @Test
  void deveRetornar400QuandoPayloadInvalidoNoCadastro() throws Exception {
    mockMvc
        .perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "nomeCompleto": "",
                  "email": "email-invalido",
                  "senha": "",
                  "tipo": null
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void deveRetornar401NoMeSemToken() throws Exception {
    mockMvc
        .perform(get("/users/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRetornarPerfilNoMeComToken() throws Exception {
    when(obterMeuPerfilUseCase.executar(any()))
        .thenReturn(new ObterMeuPerfilUseCase.Result(
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

    mockMvc
        .perform(get("/users/me").with(jwt().jwt(jwt -> jwt.subject("id-123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("maria@teste.com"));
  }

  @Test
  void deveRetornar404QuandoNaoEncontrarPerfil() throws Exception {
    when(obterMeuPerfilUseCase.executar(any())).thenThrow(new NotFoundException("Usuario nao encontrado"));

    mockMvc
        .perform(get("/users/me").with(jwt().jwt(jwt -> jwt.subject("id-404"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void deveRetornar409QuandoEmailJaCadastradoNoCadastro() throws Exception {
    when(cadastrarUsuarioUseCase.executar(any())).thenThrow(new ConflictException("Email ja cadastrado"));

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
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409));
  }

  @Test
  void deveAtualizarMeuPerfilComToken() throws Exception {
    when(atualizarMeuPerfilUseCase.executar(any()))
        .thenReturn(new AtualizarMeuPerfilUseCase.Result(
            1L,
            "id-123",
            "Maria Silva",
            "maria@teste.com",
            TipoUsuario.MORADOR,
            "11999999999",
            "12345678901",
            "101",
            "A"
        ));

    mockMvc
        .perform(put("/users/me")
            .with(jwt().jwt(jwt -> jwt.subject("id-123")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "nomeCompleto": "Maria Silva",
                  "telefone": "11999999999",
                  "cpf": "12345678901",
                  "apartamento": "101",
                  "bloco": "A"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nomeCompleto").value("Maria Silva"));
  }
}

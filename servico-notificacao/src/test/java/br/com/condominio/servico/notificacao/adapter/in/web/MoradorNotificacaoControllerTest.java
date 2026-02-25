package br.com.condominio.servico.notificacao.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.condominio.servico.notificacao.application.exception.AcessoNegadoException;
import br.com.condominio.servico.notificacao.application.exception.NotificacaoNaoEncontradaException;
import br.com.condominio.servico.notificacao.application.port.in.ConfirmarRecebimentoNotificacaoUseCase;
import br.com.condominio.servico.notificacao.application.port.in.ListarNotificacoesPendentesUseCase;
import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import br.com.condominio.servico.notificacao.infrastructure.security.SecurityConfig;
import br.com.condominio.servico.notificacao.infrastructure.web.error.GlobalExceptionHandler;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MoradorNotificacaoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
    "security.jwt.secret=condominio-jwt-secret-local-2026-seguro",
    "security.jwt.issuer=servico-identidade"
})
class MoradorNotificacaoControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ListarNotificacoesPendentesUseCase listarNotificacoesPendentesUseCase;

  @MockBean
  private ConfirmarRecebimentoNotificacaoUseCase confirmarRecebimentoNotificacaoUseCase;

  @Test
  void deveRetornar401NaListagemSemToken() throws Exception {
    mockMvc.perform(get("/morador/notificacoes"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRetornar403NaListagemComRoleInvalida() throws Exception {
    mockMvc.perform(
            get("/morador/notificacoes")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_FUNCIONARIO")))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void deveListarNotificacoesNaoConfirmadasDoMoradorLogado() throws Exception {
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

    mockMvc.perform(
            get("/morador/notificacoes")
                .param("confirmada", "false")
                .param("page", "0")
                .param("size", "10")
                .with(jwt()
                    .jwt(jwt -> jwt.subject("morador-1"))
                    .authorities(new SimpleGrantedAuthority("ROLE_MORADOR")))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.notificacoes[0].id").value("not-1"))
        .andExpect(jsonPath("$.notificacoes[0].status").value("ENVIADA"));
  }

  @Test
  void deveRetornar400QuandoSolicitarConfirmadaTrue() throws Exception {
    when(listarNotificacoesPendentesUseCase.executar(any()))
        .thenThrow(new IllegalArgumentException("Somente filtro confirmada=false e suportado"));

    mockMvc.perform(
            get("/morador/notificacoes")
                .param("confirmada", "true")
                .with(jwt()
                    .jwt(jwt -> jwt.subject("morador-1"))
                    .authorities(new SimpleGrantedAuthority("ROLE_MORADOR")))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void deveConfirmarNotificacaoDoMorador() throws Exception {
    when(confirmarRecebimentoNotificacaoUseCase.executar(any()))
        .thenReturn(new ConfirmarRecebimentoNotificacaoUseCase.Result(
            "not-1",
            StatusNotificacao.CONFIRMADA,
            Instant.parse("2026-02-25T00:02:00Z")
        ));

    mockMvc.perform(
            post("/morador/notificacoes/not-1/confirmacao")
                .with(jwt()
                    .jwt(jwt -> jwt.subject("morador-1"))
                    .authorities(new SimpleGrantedAuthority("ROLE_MORADOR")))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("not-1"))
        .andExpect(jsonPath("$.status").value("CONFIRMADA"));
  }

  @Test
  void deveRetornar404QuandoNotificacaoNaoExiste() throws Exception {
    when(confirmarRecebimentoNotificacaoUseCase.executar(any()))
        .thenThrow(new NotificacaoNaoEncontradaException("Notificacao nao encontrada"));

    mockMvc.perform(
            post("/morador/notificacoes/not-404/confirmacao")
                .with(jwt()
                    .jwt(jwt -> jwt.subject("morador-1"))
                    .authorities(new SimpleGrantedAuthority("ROLE_MORADOR")))
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void deveRetornar403QuandoNotificacaoForDeOutroMorador() throws Exception {
    when(confirmarRecebimentoNotificacaoUseCase.executar(any()))
        .thenThrow(new AcessoNegadoException("Notificacao nao pertence ao morador autenticado"));

    mockMvc.perform(
            post("/morador/notificacoes/not-2/confirmacao")
                .with(jwt()
                    .jwt(jwt -> jwt.subject("morador-1"))
                    .authorities(new SimpleGrantedAuthority("ROLE_MORADOR")))
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void deveRetornar409QuandoNotificacaoNaoPuderSerConfirmada() throws Exception {
    when(confirmarRecebimentoNotificacaoUseCase.executar(any()))
        .thenThrow(new IllegalStateException("Somente notificacao enviada pode ser confirmada"));

    mockMvc.perform(
            post("/morador/notificacoes/not-3/confirmacao")
                .with(jwt()
                    .jwt(jwt -> jwt.subject("morador-1"))
                    .authorities(new SimpleGrantedAuthority("ROLE_MORADOR")))
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409));
  }
}

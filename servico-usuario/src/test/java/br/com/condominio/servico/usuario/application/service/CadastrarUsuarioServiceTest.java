package br.com.condominio.servico.usuario.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.usuario.application.exception.ConflictException;
import br.com.condominio.servico.usuario.application.exception.PersistenciaConflitoException;
import br.com.condominio.servico.usuario.application.port.in.CadastrarUsuarioUseCase;
import br.com.condominio.servico.usuario.application.port.out.IdentityGatewayPort;
import br.com.condominio.servico.usuario.application.port.out.UsuarioRepositoryPort;
import br.com.condominio.servico.usuario.domain.TipoUsuario;
import br.com.condominio.servico.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CadastrarUsuarioServiceTest {

  @Mock
  private UsuarioRepositoryPort usuarioRepositoryPort;

  @Mock
  private IdentityGatewayPort identityGatewayPort;

  @InjectMocks
  private CadastrarUsuarioService service;

  @Test
  void deveCadastrarUsuarioMoradorComSucesso() {
    CadastrarUsuarioUseCase.Command command = new CadastrarUsuarioUseCase.Command(
        "Maria Silva",
        "maria@teste.com",
        "123456",
        TipoUsuario.MORADOR,
        "11999999999",
        "12345678901",
        "101",
        "A"
    );

    when(usuarioRepositoryPort.existePorEmail("maria@teste.com")).thenReturn(false);
    when(identityGatewayPort.criarUsuario(any()))
        .thenReturn(new IdentityGatewayPort.CriarIdentidadeResult("id-123", "maria@teste.com"));
    when(usuarioRepositoryPort.salvar(any()))
        .thenReturn(new Usuario.Builder()
            .withId(1L)
            .withIdentityId("id-123")
            .withNomeCompleto("Maria Silva")
            .withEmail("maria@teste.com")
            .withTipo(TipoUsuario.MORADOR)
            .withTelefone("11999999999")
            .withCpf("12345678901")
            .withApartamento("101")
            .withBloco("A")
            .build());

    CadastrarUsuarioUseCase.Result result = service.executar(command);

    assertEquals(1L, result.id());
    assertEquals("id-123", result.identityId());
    assertEquals("maria@teste.com", result.email());
    verify(identityGatewayPort).criarUsuario(any());
    verify(usuarioRepositoryPort).salvar(any());
  }

  @Test
  void deveLancarConflitoQuandoEmailJaExiste() {
    CadastrarUsuarioUseCase.Command command = new CadastrarUsuarioUseCase.Command(
        "Maria Silva",
        "maria@teste.com",
        "123456",
        TipoUsuario.MORADOR,
        null,
        null,
        "101",
        "A"
    );

    when(usuarioRepositoryPort.existePorEmail("maria@teste.com")).thenReturn(true);

    assertThrows(ConflictException.class, () -> service.executar(command));
  }

  @Test
  void deveCompensarRemovendoIdentidadeQuandoPersistenciaFalha() {
    CadastrarUsuarioUseCase.Command command = new CadastrarUsuarioUseCase.Command(
        "Maria Silva",
        "maria@teste.com",
        "123456",
        TipoUsuario.MORADOR,
        null,
        null,
        "101",
        "A"
    );

    when(usuarioRepositoryPort.existePorEmail("maria@teste.com")).thenReturn(false);
    when(identityGatewayPort.criarUsuario(any()))
        .thenReturn(new IdentityGatewayPort.CriarIdentidadeResult("id-123", "maria@teste.com"));
    when(usuarioRepositoryPort.salvar(any())).thenThrow(new RuntimeException("falha banco"));

    assertThrows(RuntimeException.class, () -> service.executar(command));

    verify(identityGatewayPort).removerUsuario("id-123");
  }

  @Test
  void deveRetornarConflitoQuandoSalvarFalhaPorDuplicidade() {
    CadastrarUsuarioUseCase.Command command = new CadastrarUsuarioUseCase.Command(
        "Maria Silva",
        "maria@teste.com",
        "123456",
        TipoUsuario.MORADOR,
        null,
        null,
        "101",
        "A"
    );

    when(usuarioRepositoryPort.existePorEmail("maria@teste.com")).thenReturn(false);
    when(identityGatewayPort.criarUsuario(any()))
        .thenReturn(new IdentityGatewayPort.CriarIdentidadeResult("id-123", "maria@teste.com"));
    when(usuarioRepositoryPort.salvar(any()))
        .thenThrow(new PersistenciaConflitoException("duplicado", new RuntimeException()));

    assertThrows(ConflictException.class, () -> service.executar(command));

    verify(identityGatewayPort).removerUsuario("id-123");
    verify(identityGatewayPort, never()).desabilitarUsuario("id-123");
  }

  @Test
  void deveCompensarDesabilitandoQuandoFalhaAoRemoverIdentidade() {
    CadastrarUsuarioUseCase.Command command = new CadastrarUsuarioUseCase.Command(
        "Maria Silva",
        "maria@teste.com",
        "123456",
        TipoUsuario.MORADOR,
        null,
        null,
        "101",
        "A"
    );

    when(usuarioRepositoryPort.existePorEmail("maria@teste.com")).thenReturn(false);
    when(identityGatewayPort.criarUsuario(any()))
        .thenReturn(new IdentityGatewayPort.CriarIdentidadeResult("id-123", "maria@teste.com"));
    when(usuarioRepositoryPort.salvar(any())).thenThrow(new RuntimeException("falha banco"));
    doThrow(new RuntimeException("falha remover")).when(identityGatewayPort).removerUsuario("id-123");

    assertThrows(RuntimeException.class, () -> service.executar(command));

    verify(identityGatewayPort).desabilitarUsuario("id-123");
  }
}

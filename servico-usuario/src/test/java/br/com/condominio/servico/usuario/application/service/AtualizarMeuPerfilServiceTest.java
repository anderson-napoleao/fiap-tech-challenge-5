package br.com.condominio.servico.usuario.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.usuario.application.exception.NotFoundException;
import br.com.condominio.servico.usuario.application.port.in.AtualizarMeuPerfilUseCase;
import br.com.condominio.servico.usuario.application.port.out.UsuarioRepositoryPort;
import br.com.condominio.servico.usuario.domain.TipoUsuario;
import br.com.condominio.servico.usuario.domain.Usuario;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AtualizarMeuPerfilServiceTest {

  @Mock
  private UsuarioRepositoryPort usuarioRepositoryPort;

  @InjectMocks
  private AtualizarMeuPerfilService service;

  @Test
  void deveAtualizarPerfilMoradorComSucesso() {
    Usuario existente = new Usuario.Builder()
        .withId(1L)
        .withIdentityId("id-123")
        .withNomeCompleto("Maria")
        .withEmail("maria@teste.com")
        .withTipo(TipoUsuario.MORADOR)
        .withTelefone("111")
        .withCpf("123")
        .withApartamento("101")
        .withBloco("A")
        .build();

    when(usuarioRepositoryPort.buscarPorIdentityId("id-123")).thenReturn(Optional.of(existente));
    when(usuarioRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

    AtualizarMeuPerfilUseCase.Result result = service.executar(
        "id-123",
        new AtualizarMeuPerfilUseCase.Command("Maria Silva", "222", "999", "102", "B")
    );

    assertEquals("Maria Silva", result.nomeCompleto());
    assertEquals("102", result.apartamento());
    assertEquals("B", result.bloco());
  }

  @Test
  void deveLancarNotFoundQuandoUsuarioNaoExiste() {
    when(usuarioRepositoryPort.buscarPorIdentityId("id-404")).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> service.executar("id-404", new AtualizarMeuPerfilUseCase.Command(null, null, null, "101", "A"))
    );
  }
}

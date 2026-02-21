package br.com.condominio.servico.usuario.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.usuario.application.exception.NotFoundException;
import br.com.condominio.servico.usuario.application.port.in.ObterMeuPerfilUseCase;
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
class ObterMeuPerfilServiceTest {

  @Mock
  private UsuarioRepositoryPort usuarioRepositoryPort;

  @InjectMocks
  private ObterMeuPerfilService service;

  @Test
  void deveObterPerfilComSucesso() {
    when(usuarioRepositoryPort.buscarPorIdentityId("id-123")).thenReturn(Optional.of(
        new Usuario.Builder()
            .withId(1L)
            .withIdentityId("id-123")
            .withNomeCompleto("Maria")
            .withEmail("maria@teste.com")
            .withTipo(TipoUsuario.MORADOR)
            .withTelefone(null)
            .withCpf(null)
            .withApartamento("101")
            .withBloco("A")
            .build()
    ));

    ObterMeuPerfilUseCase.Result result = service.executar(new ObterMeuPerfilUseCase.Command("id-123"));

    assertEquals("id-123", result.identityId());
    assertEquals("maria@teste.com", result.email());
  }

  @Test
  void deveLancarNotFoundQuandoNaoExistePerfil() {
    when(usuarioRepositoryPort.buscarPorIdentityId("id-404")).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> service.executar(new ObterMeuPerfilUseCase.Command("id-404"))
    );
  }
}

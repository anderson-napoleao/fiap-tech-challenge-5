package br.com.condominio.identidade.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.condominio.identidade.application.port.in.CriarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.out.UsuarioStorePort;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CriarUsuarioAdminServiceTest {

  @Mock
  private UsuarioStorePort usuarioStorePort;

  @InjectMocks
  private CriarUsuarioAdminService service;

  @Test
  void commandDeveValidarCamposSimples() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new CriarUsuarioAdminUseCase.Command("", "123", "ADMIN")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new CriarUsuarioAdminUseCase.Command("admin", "123", " ")
    );
  }

  @Test
  void deveCriarUsuarioQuandoDadosValidos() {
    CriarUsuarioAdminUseCase.Command command =
        new CriarUsuarioAdminUseCase.Command("admin@condominio.com", "123456", "ADMIN");

    when(usuarioStorePort.existsByEmail("admin@condominio.com")).thenReturn(false);
    when(usuarioStorePort.create("admin@condominio.com", "123456", "ADMIN"))
        .thenReturn(new UsuarioStorePort.IdentityUserData("id-123", "admin@condominio.com", true, Set.of("ROLE_ADMIN")));

    CriarUsuarioAdminUseCase.UsuarioResponse response = service.criar(command);

    assertEquals("id-123", response.id());
    assertEquals("admin@condominio.com", response.email());
    verify(usuarioStorePort).create("admin@condominio.com", "123456", "ADMIN");
  }

  @Test
  void deveLancarErroQuandoUsuarioJaExiste() {
    CriarUsuarioAdminUseCase.Command command =
        new CriarUsuarioAdminUseCase.Command("admin@condominio.com", "123456", "ADMIN");

    when(usuarioStorePort.existsByEmail("admin@condominio.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> service.criar(command));
  }
}

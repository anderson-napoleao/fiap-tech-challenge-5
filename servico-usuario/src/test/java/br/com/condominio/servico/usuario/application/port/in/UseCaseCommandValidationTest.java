package br.com.condominio.servico.usuario.application.port.in;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.condominio.servico.usuario.domain.TipoUsuario;
import org.junit.jupiter.api.Test;

class UseCaseCommandValidationTest {

  @Test
  void cadastrarCommandDeveValidarCamposObrigatorios() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new CadastrarUsuarioUseCase.Command(null, "a@b.com", "123", TipoUsuario.MORADOR, null, null, "101", "A")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new CadastrarUsuarioUseCase.Command("Maria", "email-invalido", "123", TipoUsuario.MORADOR, null, null, "101", "A")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new CadastrarUsuarioUseCase.Command("Maria", "a@b.com", "", TipoUsuario.MORADOR, null, null, "101", "A")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new CadastrarUsuarioUseCase.Command("Maria", "a@b.com", "123", TipoUsuario.MORADOR, " ", null, "101", "A")
    );
  }

  @Test
  void atualizarMeuPerfilCommandDeveValidarIdentityIdECamposOpcionais() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AtualizarMeuPerfilUseCase.Command(" ", "Maria", null, null, null, null)
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> new AtualizarMeuPerfilUseCase.Command("id-123", " ", null, null, null, null)
    );

    assertDoesNotThrow(
        () -> new AtualizarMeuPerfilUseCase.Command("id-123", null, "11999999999", null, null, null)
    );
  }

  @Test
  void obterMeuPerfilCommandDeveValidarIdentityId() {
    assertThrows(IllegalArgumentException.class, () -> new ObterMeuPerfilUseCase.Command(""));
    assertDoesNotThrow(() -> new ObterMeuPerfilUseCase.Command("id-123"));
  }
}

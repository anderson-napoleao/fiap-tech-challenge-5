package br.com.condominio.servico.usuario.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.usuario.application.port.in.ListarMoradoresPorUnidadeUseCase;
import br.com.condominio.servico.usuario.application.port.out.UsuarioRepositoryPort;
import br.com.condominio.servico.usuario.domain.TipoUsuario;
import br.com.condominio.servico.usuario.domain.Usuario;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListarMoradoresPorUnidadeServiceTest {

  @Mock
  private UsuarioRepositoryPort usuarioRepositoryPort;

  private ListarMoradoresPorUnidadeService service;

  @BeforeEach
  void setup() {
    service = new ListarMoradoresPorUnidadeService(usuarioRepositoryPort);
  }

  @Test
  void deveFalharQuandoComandoForNulo() {
    assertThrows(IllegalArgumentException.class, () -> service.executar(null));
  }

  @Test
  void deveListarMoradoresDaUnidade() {
    when(usuarioRepositoryPort.listarMoradoresPorUnidade("A", "101"))
        .thenReturn(List.of(
            new Usuario.Builder()
                .withIdentityId("id-1")
                .withNomeCompleto("Maria")
                .withEmail("maria@teste.com")
                .withTipo(TipoUsuario.MORADOR)
                .withApartamento("101")
                .withBloco("A")
                .build()
        ));

    ListarMoradoresPorUnidadeUseCase.Result result = service.executar(
        new ListarMoradoresPorUnidadeUseCase.Command("A", "101")
    );

    assertEquals(1, result.moradores().size());
    assertEquals("id-1", result.moradores().getFirst().identityId());
    assertEquals("Maria", result.moradores().getFirst().nomeCompleto());
  }
}


package br.com.condominio.servico.usuario.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import br.com.condominio.servico.usuario.application.service.AtualizarMeuPerfilService;
import br.com.condominio.servico.usuario.application.service.CadastrarUsuarioService;
import br.com.condominio.servico.usuario.application.service.ListarMoradoresPorUnidadeService;
import br.com.condominio.servico.usuario.application.service.ObterMeuPerfilService;
import br.com.condominio.servico.usuario.application.port.out.IdentityGatewayPort;
import br.com.condominio.servico.usuario.application.port.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BeansConfigTest {

  @Mock
  private UsuarioRepositoryPort usuarioRepositoryPort;

  @Mock
  private IdentityGatewayPort identityGatewayPort;

  @Test
  void deveCriarBeansDaCamadaDeAplicacao() {
    BeansConfig config = new BeansConfig();

    assertInstanceOf(
        CadastrarUsuarioService.class,
        config.cadastrarUsuarioUseCase(usuarioRepositoryPort, identityGatewayPort)
    );
    assertInstanceOf(
        ObterMeuPerfilService.class,
        config.obterMeuPerfilUseCase(usuarioRepositoryPort)
    );
    assertInstanceOf(
        AtualizarMeuPerfilService.class,
        config.atualizarMeuPerfilUseCase(usuarioRepositoryPort)
    );
    assertInstanceOf(
        ListarMoradoresPorUnidadeService.class,
        config.listarMoradoresPorUnidadeUseCase(usuarioRepositoryPort)
    );
  }
}


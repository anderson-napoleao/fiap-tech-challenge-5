package br.com.condominio.servico.usuario.config;

import br.com.condominio.servico.usuario.application.port.in.AtualizarMeuPerfilUseCase;
import br.com.condominio.servico.usuario.application.port.in.CadastrarUsuarioUseCase;
import br.com.condominio.servico.usuario.application.port.in.ObterMeuPerfilUseCase;
import br.com.condominio.servico.usuario.application.port.out.IdentityGatewayPort;
import br.com.condominio.servico.usuario.application.port.out.UsuarioRepositoryPort;
import br.com.condominio.servico.usuario.application.service.AtualizarMeuPerfilService;
import br.com.condominio.servico.usuario.application.service.CadastrarUsuarioService;
import br.com.condominio.servico.usuario.application.service.ObterMeuPerfilService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

  @Bean
  public CadastrarUsuarioUseCase cadastrarUsuarioUseCase(
      UsuarioRepositoryPort usuarioRepositoryPort,
      IdentityGatewayPort identityGatewayPort
  ) {
    return new CadastrarUsuarioService(usuarioRepositoryPort, identityGatewayPort);
  }

  @Bean
  public ObterMeuPerfilUseCase obterMeuPerfilUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
    return new ObterMeuPerfilService(usuarioRepositoryPort);
  }

  @Bean
  public AtualizarMeuPerfilUseCase atualizarMeuPerfilUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
    return new AtualizarMeuPerfilService(usuarioRepositoryPort);
  }
}

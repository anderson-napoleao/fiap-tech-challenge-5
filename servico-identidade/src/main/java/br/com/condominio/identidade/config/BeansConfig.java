package br.com.condominio.identidade.config;

import br.com.condominio.identidade.application.port.in.CriarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.in.DesabilitarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.in.RemoverUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.out.UsuarioStorePort;
import br.com.condominio.identidade.application.service.CriarUsuarioAdminService;
import br.com.condominio.identidade.application.service.DesabilitarUsuarioAdminService;
import br.com.condominio.identidade.application.service.RemoverUsuarioAdminService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

  @Bean
  public CriarUsuarioAdminUseCase criarUsuarioAdminUseCase(UsuarioStorePort usuarioStorePort) {
    return new CriarUsuarioAdminService(usuarioStorePort);
  }

  @Bean
  public RemoverUsuarioAdminUseCase removerUsuarioAdminUseCase(UsuarioStorePort usuarioStorePort) {
    return new RemoverUsuarioAdminService(usuarioStorePort);
  }

  @Bean
  public DesabilitarUsuarioAdminUseCase desabilitarUsuarioAdminUseCase(UsuarioStorePort usuarioStorePort) {
    return new DesabilitarUsuarioAdminService(usuarioStorePort);
  }
}

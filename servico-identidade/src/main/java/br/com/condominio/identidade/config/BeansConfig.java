package br.com.condominio.identidade.config;

import br.com.condominio.identidade.application.port.in.CriarUsuarioAdminUseCase;
import br.com.condominio.identidade.application.port.out.UsuarioStorePort;
import br.com.condominio.identidade.application.service.CriarUsuarioAdminService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

  @Bean
  public CriarUsuarioAdminUseCase criarUsuarioAdminUseCase(UsuarioStorePort usuarioStorePort) {
    return new CriarUsuarioAdminService(usuarioStorePort);
  }
}

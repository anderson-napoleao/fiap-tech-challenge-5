package br.com.condominio.servico.usuario.infrastructure.observability;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ObservabilityWebMvcConfig implements WebMvcConfigurer {

  private final HttpLoggingInterceptor httpLoggingInterceptor;

  public ObservabilityWebMvcConfig(HttpLoggingInterceptor httpLoggingInterceptor) {
    this.httpLoggingInterceptor = httpLoggingInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(httpLoggingInterceptor);
  }
}

package br.com.condominio.identidade.infrastructure.security;

import br.com.condominio.identidade.application.port.out.UsuarioStorePort;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configura autenticacao e autorizacao do servico.
 */
@Configuration
public class DefaultSecurityConfig {

  @Bean
  public SecurityFilterChain appSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.ignoringRequestMatchers("/admin/users/**", "/auth/token"))
        .authorizeHttpRequests(
            auth ->
                auth
                    .requestMatchers("/actuator/health")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/token")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/admin/users")
                    .authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/admin/users/*")
                    .authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/admin/users/*/disable")
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CommandLineRunner bootstrapAdmin(UsuarioStorePort usuarioStore) {
    return args -> {
      if (!usuarioStore.existsByUsername("admin")) {
        usuarioStore.save("admin", "admin", Set.of("ADMIN"));
      }
    };
  }
}

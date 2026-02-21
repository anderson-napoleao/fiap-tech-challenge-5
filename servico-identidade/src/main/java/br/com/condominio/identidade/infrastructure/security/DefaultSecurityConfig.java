package br.com.condominio.identidade.infrastructure.security;

import br.com.condominio.identidade.infrastructure.userstore.InMemoryUsuarioStore;
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
                    .requestMatchers(HttpMethod.POST, "/auth/token")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/admin/users")
                    .permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/admin/users/*")
                    .permitAll()
                    .requestMatchers(HttpMethod.PATCH, "/admin/users/*/disable")
                    .permitAll()
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
  public CommandLineRunner bootstrapAdmin(InMemoryUsuarioStore usuarioStore) {
    return args -> {
      if (!usuarioStore.existsByUsername("admin")) {
        usuarioStore.save("admin", "admin", Set.of("ADMIN"));
      }
    };
  }
}

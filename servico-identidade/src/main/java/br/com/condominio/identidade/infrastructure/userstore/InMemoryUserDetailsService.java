package br.com.condominio.identidade.infrastructure.userstore;

import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Implementa armazenamento de usuarios para a infraestrutura de identidade.
 */
@Component
public class InMemoryUserDetailsService implements UserDetailsService {

  private final InMemoryUsuarioStore usuarioStore;

  public InMemoryUserDetailsService(InMemoryUsuarioStore usuarioStore) {
    this.usuarioStore = usuarioStore;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    InMemoryUsuarioStore.UsuarioData usuario =
        usuarioStore
            .findUsuarioByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("usuario nao encontrado: " + username));

    return User.withUsername(usuario.username())
        .password(usuario.passwordEncoded())
        .authorities(
            usuario.roles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()))
        .disabled(!usuario.enabled())
        .build();
  }
}

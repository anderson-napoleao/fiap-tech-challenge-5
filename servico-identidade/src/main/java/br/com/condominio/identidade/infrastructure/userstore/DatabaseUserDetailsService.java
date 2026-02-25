package br.com.condominio.identidade.infrastructure.userstore;

import br.com.condominio.identidade.infrastructure.persistence.entity.IdentityUserEntity;
import br.com.condominio.identidade.infrastructure.persistence.repository.SpringDataIdentityUserRepository;
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
public class DatabaseUserDetailsService implements UserDetailsService {

  private final SpringDataIdentityUserRepository usuarioRepository;

  public DatabaseUserDetailsService(SpringDataIdentityUserRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    IdentityUserEntity usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("usuario nao encontrado: " + username));

    return User.withUsername(usuario.getUsername())
        .password(usuario.getPasswordHash())
        .authorities(usuario.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()))
        .disabled(!usuario.isEnabled())
        .build();
  }
}

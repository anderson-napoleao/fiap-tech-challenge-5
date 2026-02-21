package br.com.condominio.servico.usuario.application.port.out;

import br.com.condominio.servico.usuario.domain.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {

  Usuario salvar(Usuario usuario);

  Optional<Usuario> buscarPorIdentityId(String identityId);

  boolean existePorEmail(String email);
}

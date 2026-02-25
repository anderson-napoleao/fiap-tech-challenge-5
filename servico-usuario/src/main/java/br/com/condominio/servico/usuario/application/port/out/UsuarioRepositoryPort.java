package br.com.condominio.servico.usuario.application.port.out;

import br.com.condominio.servico.usuario.domain.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Define o contrato de saida da aplicacao para integracoes externas.
 */
public interface UsuarioRepositoryPort {

  Usuario salvar(Usuario usuario);

  Optional<Usuario> buscarPorIdentityId(String identityId);

  List<Usuario> listarMoradoresPorUnidade(String bloco, String apartamento);

  boolean existePorEmail(String email);
}

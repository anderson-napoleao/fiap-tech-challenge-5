package br.com.condominio.servico.usuario.application.service;

import br.com.condominio.servico.usuario.application.port.in.ListarMoradoresPorUnidadeUseCase;
import br.com.condominio.servico.usuario.application.port.out.UsuarioRepositoryPort;
import br.com.condominio.servico.usuario.domain.Usuario;
import java.util.List;
import java.util.Objects;

/**
 * Implementa a orquestracao de regras da camada de aplicacao.
 */
public class ListarMoradoresPorUnidadeService implements ListarMoradoresPorUnidadeUseCase {

  private final UsuarioRepositoryPort usuarioRepositoryPort;

  public ListarMoradoresPorUnidadeService(UsuarioRepositoryPort usuarioRepositoryPort) {
    this.usuarioRepositoryPort = Objects.requireNonNull(usuarioRepositoryPort, "Porta de usuario obrigatoria");
  }

  @Override
  public Result executar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("Comando obrigatorio");
    }

    List<Usuario> moradores = usuarioRepositoryPort.listarMoradoresPorUnidade(
        command.bloco(),
        command.apartamento()
    );

    List<Item> itens = moradores.stream()
        .map(usuario -> new Item(
            usuario.identityId(),
            usuario.nomeCompleto(),
            usuario.email()
        ))
        .toList();

    return new Result(itens);
  }
}

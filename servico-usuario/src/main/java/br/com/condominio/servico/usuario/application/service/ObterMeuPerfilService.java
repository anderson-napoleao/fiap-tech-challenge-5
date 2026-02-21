package br.com.condominio.servico.usuario.application.service;

import br.com.condominio.servico.usuario.application.exception.NotFoundException;
import br.com.condominio.servico.usuario.application.port.in.ObterMeuPerfilUseCase;
import br.com.condominio.servico.usuario.application.port.out.UsuarioRepositoryPort;
import br.com.condominio.servico.usuario.domain.Usuario;

public class ObterMeuPerfilService implements ObterMeuPerfilUseCase {

  private final UsuarioRepositoryPort usuarioRepositoryPort;

  public ObterMeuPerfilService(UsuarioRepositoryPort usuarioRepositoryPort) {
    this.usuarioRepositoryPort = usuarioRepositoryPort;
  }

  @Override
  public Result executar(String identityId) {
    if (identityId == null || identityId.isBlank()) {
      throw new IllegalArgumentException("IdentityId obrigatorio");
    }

    Usuario usuario = usuarioRepositoryPort
        .buscarPorIdentityId(identityId)
        .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

    return new Result(
        usuario.id(),
        usuario.identityId(),
        usuario.nomeCompleto(),
        usuario.email(),
        usuario.tipo(),
        usuario.telefone(),
        usuario.cpf(),
        usuario.apartamento(),
        usuario.bloco()
    );
  }
}

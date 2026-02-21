package br.com.condominio.servico.usuario.application.service;

import br.com.condominio.servico.usuario.application.exception.NotFoundException;
import br.com.condominio.servico.usuario.application.port.in.AtualizarMeuPerfilUseCase;
import br.com.condominio.servico.usuario.application.port.out.UsuarioRepositoryPort;
import br.com.condominio.servico.usuario.domain.TipoUsuario;
import br.com.condominio.servico.usuario.domain.Usuario;

public class AtualizarMeuPerfilService implements AtualizarMeuPerfilUseCase {

  private final UsuarioRepositoryPort usuarioRepositoryPort;

  public AtualizarMeuPerfilService(UsuarioRepositoryPort usuarioRepositoryPort) {
    this.usuarioRepositoryPort = usuarioRepositoryPort;
  }

  @Override
  public Result executar(String identityId, Command command) {
    if (identityId == null || identityId.isBlank()) {
      throw new IllegalArgumentException("IdentityId obrigatorio");
    }

    if (command == null) {
      throw new IllegalArgumentException("Comando obrigatorio");
    }

    Usuario existente = usuarioRepositoryPort
        .buscarPorIdentityId(identityId)
        .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

    // A entidade Usuario fará a validação de consistência no construtor

    Usuario atualizado = new Usuario.Builder(existente)
        .withNomeCompleto(nvl(command.nomeCompleto(), existente.nomeCompleto()))
        .withTelefone(nvl(command.telefone(), existente.telefone()))
        .withCpf(nvl(command.cpf(), existente.cpf()))
        .withApartamento(existente.tipo() == TipoUsuario.MORADOR ? 
            nvl(command.apartamento(), existente.apartamento()) : null)
        .withBloco(existente.tipo() == TipoUsuario.MORADOR ? 
            nvl(command.bloco(), existente.bloco()) : null)
        .build();

    Usuario salvo = usuarioRepositoryPort.salvar(atualizado);

    return new Result(
        salvo.id(),
        salvo.identityId(),
        salvo.nomeCompleto(),
        salvo.email(),
        salvo.tipo(),
        salvo.telefone(),
        salvo.cpf(),
        salvo.apartamento(),
        salvo.bloco()
    );
  }

  private String nvl(String novoValor, String valorAtual) {
    return novoValor == null || novoValor.isBlank() ? valorAtual : novoValor;
  }
}

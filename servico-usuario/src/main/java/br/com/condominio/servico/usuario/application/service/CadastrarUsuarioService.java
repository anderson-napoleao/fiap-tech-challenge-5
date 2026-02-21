package br.com.condominio.servico.usuario.application.service;

import br.com.condominio.servico.usuario.application.exception.ConflictException;
import br.com.condominio.servico.usuario.application.exception.PersistenciaConflitoException;
import br.com.condominio.servico.usuario.application.port.in.CadastrarUsuarioUseCase;
import br.com.condominio.servico.usuario.application.port.out.IdentityGatewayPort;
import br.com.condominio.servico.usuario.application.port.out.UsuarioRepositoryPort;
import br.com.condominio.servico.usuario.domain.Usuario;

public class CadastrarUsuarioService implements CadastrarUsuarioUseCase {

  private static final String ROLE_USER = "USER";

  private final UsuarioRepositoryPort usuarioRepositoryPort;
  private final IdentityGatewayPort identityGatewayPort;

  public CadastrarUsuarioService(
      UsuarioRepositoryPort usuarioRepositoryPort,
      IdentityGatewayPort identityGatewayPort
  ) {
    this.usuarioRepositoryPort = usuarioRepositoryPort;
    this.identityGatewayPort = identityGatewayPort;
  }

  @Override
  public Result executar(Command command) {
    if (command == null) {
      throw new IllegalArgumentException("Comando obrigatorio");
    }

    Usuario usuario = criarUsuario(command);

    if (usuarioRepositoryPort.existePorEmail(usuario.email())) {
      throw new ConflictException("Email ja cadastrado");
    }

    IdentityGatewayPort.CriarIdentidadeResult identity = identityGatewayPort.criarUsuario(
        new IdentityGatewayPort.CriarIdentidadeCommand(usuario.email(), command.senha(), ROLE_USER)
    );

    usuario.atribuirIdentityId(identity.identityId());

    try {
      Usuario salvo = usuarioRepositoryPort.salvar(usuario);
      return toResult(salvo);
    } catch (PersistenciaConflitoException exception) {
      compensar(identity.identityId());
      throw new ConflictException("Email ja cadastrado");
    } catch (RuntimeException exception) {
      compensar(identity.identityId());
      throw exception;
    }
  }

  private Usuario criarUsuario(Command command) {
    return new Usuario.Builder()
        .withNomeCompleto(command.nomeCompleto())
        .withEmail(command.email())
        .withTipo(command.tipo())
        .withTelefone(command.telefone())
        .withCpf(command.cpf())
        .withApartamento(command.apartamento())
        .withBloco(command.bloco())
        .build();
  }

  private void compensar(String identityId) {
    try {
      identityGatewayPort.removerUsuario(identityId);
    } catch (RuntimeException removerException) {
      identityGatewayPort.desabilitarUsuario(identityId);
    }
  }

  private Result toResult(Usuario usuario) {
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

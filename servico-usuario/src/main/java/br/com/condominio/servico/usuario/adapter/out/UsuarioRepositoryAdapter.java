package br.com.condominio.servico.usuario.adapter.out;

import br.com.condominio.servico.usuario.application.exception.PersistenciaConflitoException;
import br.com.condominio.servico.usuario.application.port.out.UsuarioRepositoryPort;
import br.com.condominio.servico.usuario.domain.Usuario;
import br.com.condominio.servico.usuario.domain.TipoUsuario;
import br.com.condominio.servico.usuario.infrastructure.persistence.entity.UsuarioEntity;
import br.com.condominio.servico.usuario.infrastructure.persistence.repository.SpringDataUsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * Adaptador de saida para persistencia ou integracao externa.
 */
@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

  private final SpringDataUsuarioRepository repository;

  public UsuarioRepositoryAdapter(SpringDataUsuarioRepository repository) {
    this.repository = repository;
  }

  @Override
  public Usuario salvar(Usuario usuario) {
    try {
      UsuarioEntity entity = toEntity(usuario);
      UsuarioEntity saved = repository.save(entity);
      return toDomain(saved);
    } catch (DataIntegrityViolationException exception) {
      throw new PersistenciaConflitoException("Conflito de persistencia", exception);
    }
  }

  @Override
  public Optional<Usuario> buscarPorIdentityId(String identityId) {
    return repository.findByIdentityId(identityId).map(this::toDomain);
  }

  @Override
  public List<Usuario> listarMoradoresPorUnidade(String bloco, String apartamento) {
    return repository.findByBlocoIgnoreCaseAndApartamentoIgnoreCaseAndTipo(
            bloco,
            apartamento,
            TipoUsuario.MORADOR
        )
        .stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public boolean existePorEmail(String email) {
    return repository.existsByEmail(email);
  }

  private UsuarioEntity toEntity(Usuario usuario) {
    UsuarioEntity entity = new UsuarioEntity();
    entity.setId(usuario.id());
    entity.setIdentityId(usuario.identityId());
    entity.setNomeCompleto(usuario.nomeCompleto());
    entity.setEmail(usuario.email());
    entity.setTipo(usuario.tipo());
    entity.setTelefone(usuario.telefone());
    entity.setCpf(usuario.cpf());
    entity.setApartamento(usuario.apartamento());
    entity.setBloco(usuario.bloco());
    return entity;
  }

  private Usuario toDomain(UsuarioEntity entity) {
    return new Usuario.Builder()
        .withId(entity.getId())
        .withIdentityId(entity.getIdentityId())
        .withNomeCompleto(entity.getNomeCompleto())
        .withEmail(entity.getEmail())
        .withTipo(entity.getTipo())
        .withTelefone(entity.getTelefone())
        .withCpf(entity.getCpf())
        .withApartamento(entity.getApartamento())
        .withBloco(entity.getBloco())
        .build();
  }
}

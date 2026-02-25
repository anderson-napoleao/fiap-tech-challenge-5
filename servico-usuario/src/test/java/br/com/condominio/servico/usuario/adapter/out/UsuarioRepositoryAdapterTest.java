package br.com.condominio.servico.usuario.adapter.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.condominio.servico.usuario.application.exception.PersistenciaConflitoException;
import br.com.condominio.servico.usuario.domain.TipoUsuario;
import br.com.condominio.servico.usuario.domain.Usuario;
import br.com.condominio.servico.usuario.infrastructure.persistence.entity.UsuarioEntity;
import br.com.condominio.servico.usuario.infrastructure.persistence.repository.SpringDataUsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class UsuarioRepositoryAdapterTest {

  @Mock
  private SpringDataUsuarioRepository repository;

  private UsuarioRepositoryAdapter adapter;

  @BeforeEach
  void setup() {
    adapter = new UsuarioRepositoryAdapter(repository);
  }

  @Test
  void deveSalvarUsuarioEMapearResultado() {
    Usuario usuario = new Usuario.Builder()
        .withId(10L)
        .withIdentityId("id-10")
        .withNomeCompleto("Maria")
        .withEmail("maria@teste.com")
        .withTipo(TipoUsuario.MORADOR)
        .withTelefone("1199999")
        .withCpf("11111111111")
        .withApartamento("101")
        .withBloco("A")
        .build();

    UsuarioEntity entity = new UsuarioEntity();
    entity.setId(10L);
    entity.setIdentityId("id-10");
    entity.setNomeCompleto("Maria");
    entity.setEmail("maria@teste.com");
    entity.setTipo(TipoUsuario.MORADOR);
    entity.setTelefone("1199999");
    entity.setCpf("11111111111");
    entity.setApartamento("101");
    entity.setBloco("A");
    when(repository.save(any(UsuarioEntity.class))).thenReturn(entity);

    Usuario salvo = adapter.salvar(usuario);

    ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
    verify(repository).save(captor.capture());
    assertEquals("id-10", captor.getValue().getIdentityId());
    assertEquals("Maria", salvo.nomeCompleto());
    assertEquals(TipoUsuario.MORADOR, salvo.tipo());
  }

  @Test
  void deveLancarConflitoQuandoRepositorioRetornaViolacaoDeIntegridade() {
    when(repository.save(any(UsuarioEntity.class))).thenThrow(new DataIntegrityViolationException("duplicado"));

    Usuario usuario = new Usuario.Builder()
        .withIdentityId("id-10")
        .withNomeCompleto("Maria")
        .withEmail("maria@teste.com")
        .withTipo(TipoUsuario.FUNCIONARIO)
        .build();

    assertThrows(PersistenciaConflitoException.class, () -> adapter.salvar(usuario));
  }

  @Test
  void deveBuscarPorIdentityId() {
    UsuarioEntity entity = new UsuarioEntity();
    entity.setId(10L);
    entity.setIdentityId("id-10");
    entity.setNomeCompleto("Maria");
    entity.setEmail("maria@teste.com");
    entity.setTipo(TipoUsuario.FUNCIONARIO);
    when(repository.findByIdentityId("id-10")).thenReturn(Optional.of(entity));

    Optional<Usuario> encontrado = adapter.buscarPorIdentityId("id-10");

    assertTrue(encontrado.isPresent());
    assertEquals("Maria", encontrado.orElseThrow().nomeCompleto());
  }

  @Test
  void deveListarMoradoresPorUnidade() {
    UsuarioEntity entity = new UsuarioEntity();
    entity.setId(10L);
    entity.setIdentityId("id-10");
    entity.setNomeCompleto("Maria");
    entity.setEmail("maria@teste.com");
    entity.setTipo(TipoUsuario.MORADOR);
    entity.setApartamento("101");
    entity.setBloco("A");

    when(repository.findByBlocoIgnoreCaseAndApartamentoIgnoreCaseAndTipo("A", "101", TipoUsuario.MORADOR))
        .thenReturn(List.of(entity));

    List<Usuario> moradores = adapter.listarMoradoresPorUnidade("A", "101");

    assertEquals(1, moradores.size());
    assertEquals("id-10", moradores.getFirst().identityId());
  }

  @Test
  void deveConsultarExistenciaPorEmail() {
    when(repository.existsByEmail("maria@teste.com")).thenReturn(true);

    assertTrue(adapter.existePorEmail("maria@teste.com"));
  }
}


package br.com.condominio.servico.usuario.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.condominio.servico.usuario.domain.TipoUsuario;
import org.junit.jupiter.api.Test;

class UsuarioEntityTest {

  @Test
  void deveSetarEObterTodosOsCampos() {
    UsuarioEntity entity = new UsuarioEntity();

    entity.setId(1L);
    entity.setIdentityId("id-1");
    entity.setNomeCompleto("Maria");
    entity.setEmail("maria@teste.com");
    entity.setTipo(TipoUsuario.MORADOR);
    entity.setTelefone("1199999999");
    entity.setCpf("12345678900");
    entity.setApartamento("101");
    entity.setBloco("A");

    assertEquals(1L, entity.getId());
    assertEquals("id-1", entity.getIdentityId());
    assertEquals("Maria", entity.getNomeCompleto());
    assertEquals("maria@teste.com", entity.getEmail());
    assertEquals(TipoUsuario.MORADOR, entity.getTipo());
    assertEquals("1199999999", entity.getTelefone());
    assertEquals("12345678900", entity.getCpf());
    assertEquals("101", entity.getApartamento());
    assertEquals("A", entity.getBloco());
  }
}


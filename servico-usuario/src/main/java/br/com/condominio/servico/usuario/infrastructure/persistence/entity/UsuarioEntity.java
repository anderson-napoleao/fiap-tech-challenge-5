package br.com.condominio.servico.usuario.infrastructure.persistence.entity;

import br.com.condominio.servico.usuario.domain.TipoUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "identity_id", nullable = false, unique = true, length = 100)
  private String identityId;

  @Column(name = "nome_completo", nullable = false, length = 150)
  private String nomeCompleto;

  @Column(name = "email", nullable = false, unique = true, length = 150)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo", nullable = false, length = 20)
  private TipoUsuario tipo;

  @Column(name = "telefone", length = 30)
  private String telefone;

  @Column(name = "cpf", length = 20)
  private String cpf;

  @Column(name = "apartamento", length = 20)
  private String apartamento;

  @Column(name = "bloco", length = 20)
  private String bloco;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getIdentityId() {
    return identityId;
  }

  public void setIdentityId(String identityId) {
    this.identityId = identityId;
  }

  public String getNomeCompleto() {
    return nomeCompleto;
  }

  public void setNomeCompleto(String nomeCompleto) {
    this.nomeCompleto = nomeCompleto;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public TipoUsuario getTipo() {
    return tipo;
  }

  public void setTipo(TipoUsuario tipo) {
    this.tipo = tipo;
  }

  public String getTelefone() {
    return telefone;
  }

  public void setTelefone(String telefone) {
    this.telefone = telefone;
  }

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    this.cpf = cpf;
  }

  public String getApartamento() {
    return apartamento;
  }

  public void setApartamento(String apartamento) {
    this.apartamento = apartamento;
  }

  public String getBloco() {
    return bloco;
  }

  public void setBloco(String bloco) {
    this.bloco = bloco;
  }
}

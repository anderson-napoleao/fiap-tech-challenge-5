package br.com.condominio.servico.usuario.domain;

/**
 * Representa regra e comportamento de negocio do dominio.
 */
public class Usuario {
  private Long id;
  private String identityId;
  private final String nomeCompleto;
  private final String email;
  private final TipoUsuario tipo;
  private final String telefone;
  private final String cpf;
  private final String apartamento;
  private final String bloco;

  private Usuario(Builder builder) {
    this.id = builder.id;
    this.identityId = builder.identityId;
    this.nomeCompleto = builder.nomeCompleto;
    this.email = builder.email;
    this.tipo = builder.tipo;
    this.telefone = builder.telefone;
    this.cpf = builder.cpf;
    this.apartamento = builder.apartamento;
    this.bloco = builder.bloco;
    validarConsistencia();
  }

  private void validarConsistencia() {
    // Regras de negÃ³cio do domÃ­nio - Rich Domain Pattern
    if (tipo == TipoUsuario.MORADOR) {
      if (apartamento == null || apartamento.isBlank()) {
        throw new IllegalArgumentException("Apartamento obrigatorio para MORADOR");
      }
      if (bloco == null || bloco.isBlank()) {
        throw new IllegalArgumentException("Bloco obrigatorio para MORADOR");
      }
    }
    
    if (tipo == TipoUsuario.FUNCIONARIO) {
      // FUNCIONARIO nÃ£o deve ter apartamento/bloco
      if (apartamento != null && !apartamento.isBlank()) {
        throw new IllegalArgumentException("FUNCIONARIO nao deve ter apartamento");
      }
      if (bloco != null && !bloco.isBlank()) {
        throw new IllegalArgumentException("FUNCIONARIO nao deve ter bloco");
      }
    }
  }

  // Getters
  public Long id() { return id; }
  public String identityId() { return identityId; }
  public String nomeCompleto() { return nomeCompleto; }
  public String email() { return email; }
  public TipoUsuario tipo() { return tipo; }
  public String telefone() { return telefone; }
  public String cpf() { return cpf; }
  public String apartamento() { return apartamento; }
  public String bloco() { return bloco; }

  public Usuario atribuirIdentityId(String identityId) {
    if (identityId == null || identityId.isBlank()) {
      throw new IllegalArgumentException("IdentityId obrigatorio");
    }
    this.identityId = identityId;
    return this;
  }

  // Builder pattern
  public static class Builder {
    private Long id;
    private String identityId;
    private String nomeCompleto;
    private String email;
    private TipoUsuario tipo;
    private String telefone;
    private String cpf;
    private String apartamento;
    private String bloco;

    public Builder() {}

    public Builder(Usuario usuario) {
      this.id = usuario.id;
      this.identityId = usuario.identityId;
      this.nomeCompleto = usuario.nomeCompleto;
      this.email = usuario.email;
      this.tipo = usuario.tipo;
      this.telefone = usuario.telefone;
      this.cpf = usuario.cpf;
      this.apartamento = usuario.apartamento;
      this.bloco = usuario.bloco;
    }

    public Builder withId(Long id) {
      this.id = id;
      return this;
    }

    public Builder withIdentityId(String identityId) {
      this.identityId = identityId;
      return this;
    }

    public Builder withNomeCompleto(String nomeCompleto) {
      this.nomeCompleto = nomeCompleto;
      return this;
    }

    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    public Builder withTipo(TipoUsuario tipo) {
      this.tipo = tipo;
      return this;
    }

    public Builder withTelefone(String telefone) {
      this.telefone = telefone;
      return this;
    }

    public Builder withCpf(String cpf) {
      this.cpf = cpf;
      return this;
    }

    public Builder withApartamento(String apartamento) {
      this.apartamento = apartamento;
      return this;
    }

    public Builder withBloco(String bloco) {
      this.bloco = bloco;
      return this;
    }

    public Usuario build() {
      return new Usuario(this);
    }
  }
}

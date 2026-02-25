package br.com.condominio.servico.encomenda.infrastructure.persistence.entity;

import br.com.condominio.servico.encomenda.domain.StatusEncomenda;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Entidade de persistencia mapeada para o banco de dados.
 */
@Entity
@Table(name = "encomendas")
public class EncomendaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nome_destinatario", nullable = false, length = 150)
  private String nomeDestinatario;

  @Column(name = "apartamento", nullable = false, length = 20)
  private String apartamento;

  @Column(name = "bloco", nullable = false, length = 20)
  private String bloco;

  @Column(name = "descricao", nullable = false, length = 500)
  private String descricao;

  @Column(name = "recebido_por", nullable = false, length = 120)
  private String recebidoPor;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private StatusEncomenda status;

  @Column(name = "data_recebimento", nullable = false)
  private Instant dataRecebimento;

  @Column(name = "data_retirada")
  private Instant dataRetirada;

  @Column(name = "retirado_por_nome", length = 150)
  private String retiradoPorNome;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNomeDestinatario() {
    return nomeDestinatario;
  }

  public void setNomeDestinatario(String nomeDestinatario) {
    this.nomeDestinatario = nomeDestinatario;
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

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public String getRecebidoPor() {
    return recebidoPor;
  }

  public void setRecebidoPor(String recebidoPor) {
    this.recebidoPor = recebidoPor;
  }

  public StatusEncomenda getStatus() {
    return status;
  }

  public void setStatus(StatusEncomenda status) {
    this.status = status;
  }

  public Instant getDataRecebimento() {
    return dataRecebimento;
  }

  public void setDataRecebimento(Instant dataRecebimento) {
    this.dataRecebimento = dataRecebimento;
  }

  public Instant getDataRetirada() {
    return dataRetirada;
  }

  public void setDataRetirada(Instant dataRetirada) {
    this.dataRetirada = dataRetirada;
  }

  public String getRetiradoPorNome() {
    return retiradoPorNome;
  }

  public void setRetiradoPorNome(String retiradoPorNome) {
    this.retiradoPorNome = retiradoPorNome;
  }
}

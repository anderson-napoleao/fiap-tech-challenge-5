package br.com.condominio.servico.encomenda.domain;

import java.time.Instant;

public class Encomenda {

  private Long id;
  private final String nomeDestinatario;
  private final String apartamento;
  private final String bloco;
  private final String descricao;
  private final String recebidoPor;
  private final Instant dataRecebimento;
  private StatusEncomenda status;
  private Instant dataRetirada;

  private Encomenda(
      String nomeDestinatario,
      String apartamento,
      String bloco,
      String descricao,
      String recebidoPor,
      Instant dataRecebimento
  ) {
    this.nomeDestinatario = validarObrigatorio(nomeDestinatario, "Nome do destinatario obrigatorio");
    this.apartamento = validarObrigatorio(apartamento, "Apartamento obrigatorio");
    this.bloco = validarObrigatorio(bloco, "Bloco obrigatorio");
    this.descricao = validarObrigatorio(descricao, "Descricao obrigatoria");
    this.recebidoPor = validarObrigatorio(recebidoPor, "Recebido por obrigatorio");
    this.dataRecebimento = validarInstante(dataRecebimento, "Data de recebimento obrigatoria");
    this.status = StatusEncomenda.RECEBIDA;
  }

  public static Encomenda receber(
      String nomeDestinatario,
      String apartamento,
      String bloco,
      String descricao,
      String recebidoPor,
      Instant dataRecebimento
  ) {
    return new Encomenda(nomeDestinatario, apartamento, bloco, descricao, recebidoPor, dataRecebimento);
  }

  public void marcarRetirada(Instant dataRetirada) {
    validarInstante(dataRetirada, "Data de retirada obrigatoria");

    if (status != StatusEncomenda.RECEBIDA) {
      throw new IllegalStateException("Somente encomenda RECEBIDA pode ser retirada");
    }

    if (dataRetirada.isBefore(dataRecebimento)) {
      throw new IllegalArgumentException("Data de retirada invalida");
    }

    this.status = StatusEncomenda.RETIRADA;
    this.dataRetirada = dataRetirada;
  }

  public Encomenda atribuirId(Long id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("Id invalido");
    }
    this.id = id;
    return this;
  }

  public Long id() {
    return id;
  }

  public String nomeDestinatario() {
    return nomeDestinatario;
  }

  public String apartamento() {
    return apartamento;
  }

  public String bloco() {
    return bloco;
  }

  public String descricao() {
    return descricao;
  }

  public String recebidoPor() {
    return recebidoPor;
  }

  public Instant dataRecebimento() {
    return dataRecebimento;
  }

  public StatusEncomenda status() {
    return status;
  }

  public Instant dataRetirada() {
    return dataRetirada;
  }

  private static String validarObrigatorio(String valor, String mensagem) {
    if (valor == null || valor.isBlank()) {
      throw new IllegalArgumentException(mensagem);
    }
    return valor;
  }

  private static Instant validarInstante(Instant valor, String mensagem) {
    if (valor == null) {
      throw new IllegalArgumentException(mensagem);
    }
    return valor;
  }
}

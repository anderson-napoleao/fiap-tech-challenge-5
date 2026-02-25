package br.com.condominio.servico.notificacao.domain;

import java.time.Instant;

public class Notificacao {

  private String id;
  private final String encomendaId;
  private final String moradorId;
  private final CanalNotificacao canal;
  private final String destino;
  private final String mensagem;
  private final String sourceEventId;
  private final String correlationId;
  private final Instant criadaEm;
  private StatusNotificacao status;
  private Instant enviadaEm;
  private Instant confirmadaEm;
  private Instant falhaEm;
  private String motivoFalha;

  private Notificacao(
      String encomendaId,
      String moradorId,
      CanalNotificacao canal,
      String destino,
      String mensagem,
      String sourceEventId,
      String correlationId,
      Instant criadaEm
  ) {
    this.encomendaId = validarObrigatorio(encomendaId, "Encomenda obrigatoria");
    this.moradorId = validarObrigatorio(moradorId, "Morador obrigatorio");
    this.canal = validarCanal(canal);
    this.destino = validarObrigatorio(destino, "Destino obrigatorio");
    this.mensagem = validarObrigatorio(mensagem, "Mensagem obrigatoria");
    this.sourceEventId = validarObrigatorio(sourceEventId, "Source event id obrigatorio");
    this.correlationId = validarObrigatorio(correlationId, "Correlation id obrigatorio");
    this.criadaEm = validarInstante(criadaEm, "Data de criacao obrigatoria");
    this.status = StatusNotificacao.PENDENTE;
  }

  public static Notificacao criar(
      String encomendaId,
      String moradorId,
      CanalNotificacao canal,
      String destino,
      String mensagem,
      String sourceEventId,
      String correlationId,
      Instant criadaEm
  ) {
    return new Notificacao(
        encomendaId,
        moradorId,
        canal,
        destino,
        mensagem,
        sourceEventId,
        correlationId,
        criadaEm
    );
  }

  public void marcarEnviada(Instant enviadaEm) {
    validarInstante(enviadaEm, "Data de envio obrigatoria");

    if (status == StatusNotificacao.CONFIRMADA) {
      throw new IllegalStateException("Notificacao confirmada nao pode voltar para enviada");
    }

    if (status == StatusNotificacao.ENVIADA) {
      return;
    }

    if (status != StatusNotificacao.PENDENTE && status != StatusNotificacao.FALHA) {
      throw new IllegalStateException("Somente notificacao pendente ou com falha pode ser enviada");
    }

    this.status = StatusNotificacao.ENVIADA;
    this.enviadaEm = enviadaEm;
    this.falhaEm = null;
    this.motivoFalha = null;
  }

  public void marcarFalha(String motivoFalha, Instant falhaEm) {
    validarObrigatorio(motivoFalha, "Motivo de falha obrigatorio");
    validarInstante(falhaEm, "Data de falha obrigatoria");

    if (status == StatusNotificacao.CONFIRMADA) {
      throw new IllegalStateException("Notificacao confirmada nao pode receber falha");
    }

    this.status = StatusNotificacao.FALHA;
    this.motivoFalha = motivoFalha;
    this.falhaEm = falhaEm;
  }

  public void confirmarRecebimento(String moradorId, Instant confirmadaEm) {
    String moradorInformado = validarObrigatorio(moradorId, "Morador obrigatorio");
    validarInstante(confirmadaEm, "Data de confirmacao obrigatoria");

    if (!this.moradorId.equals(moradorInformado)) {
      throw new IllegalArgumentException("Notificacao nao pertence ao morador informado");
    }

    if (status == StatusNotificacao.CONFIRMADA) {
      return;
    }

    if (status != StatusNotificacao.ENVIADA) {
      throw new IllegalStateException("Somente notificacao enviada pode ser confirmada");
    }

    this.status = StatusNotificacao.CONFIRMADA;
    this.confirmadaEm = confirmadaEm;
  }

  public Notificacao atribuirId(String id) {
    this.id = validarObrigatorio(id, "Id obrigatorio");
    return this;
  }

  public String id() {
    return id;
  }

  public String encomendaId() {
    return encomendaId;
  }

  public String moradorId() {
    return moradorId;
  }

  public CanalNotificacao canal() {
    return canal;
  }

  public String destino() {
    return destino;
  }

  public String mensagem() {
    return mensagem;
  }

  public String sourceEventId() {
    return sourceEventId;
  }

  public String correlationId() {
    return correlationId;
  }

  public Instant criadaEm() {
    return criadaEm;
  }

  public StatusNotificacao status() {
    return status;
  }

  public Instant enviadaEm() {
    return enviadaEm;
  }

  public Instant confirmadaEm() {
    return confirmadaEm;
  }

  public Instant falhaEm() {
    return falhaEm;
  }

  public String motivoFalha() {
    return motivoFalha;
  }

  private static String validarObrigatorio(String valor, String mensagem) {
    if (valor == null || valor.isBlank()) {
      throw new IllegalArgumentException(mensagem);
    }
    return valor;
  }

  private static CanalNotificacao validarCanal(CanalNotificacao canal) {
    if (canal == null) {
      throw new IllegalArgumentException("Canal obrigatorio");
    }
    return canal;
  }

  private static Instant validarInstante(Instant valor, String mensagem) {
    if (valor == null) {
      throw new IllegalArgumentException(mensagem);
    }
    return valor;
  }
}

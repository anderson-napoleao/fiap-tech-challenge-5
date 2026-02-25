package br.com.condominio.servico.notificacao.infrastructure.persistence.entity;

import br.com.condominio.servico.notificacao.domain.CanalNotificacao;
import br.com.condominio.servico.notificacao.domain.StatusNotificacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notificacoes")
public class NotificacaoEntity {

  @Id
  @Column(name = "id", nullable = false, length = 36)
  private String id;

  @Column(name = "encomenda_id", nullable = false, length = 64)
  private String encomendaId;

  @Column(name = "morador_id", nullable = false, length = 64)
  private String moradorId;

  @Enumerated(EnumType.STRING)
  @Column(name = "canal", nullable = false, length = 20)
  private CanalNotificacao canal;

  @Column(name = "destino", nullable = false, length = 255)
  private String destino;

  @Column(name = "mensagem", nullable = false, length = 1000)
  private String mensagem;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private StatusNotificacao status;

  @Column(name = "source_event_id", nullable = false, length = 64, unique = true)
  private String sourceEventId;

  @Column(name = "correlation_id", nullable = false, length = 64)
  private String correlationId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "confirmed_at")
  private Instant confirmedAt;

  @Column(name = "failed_at")
  private Instant failedAt;

  @Column(name = "failure_reason", length = 500)
  private String failureReason;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEncomendaId() {
    return encomendaId;
  }

  public void setEncomendaId(String encomendaId) {
    this.encomendaId = encomendaId;
  }

  public String getMoradorId() {
    return moradorId;
  }

  public void setMoradorId(String moradorId) {
    this.moradorId = moradorId;
  }

  public CanalNotificacao getCanal() {
    return canal;
  }

  public void setCanal(CanalNotificacao canal) {
    this.canal = canal;
  }

  public String getDestino() {
    return destino;
  }

  public void setDestino(String destino) {
    this.destino = destino;
  }

  public String getMensagem() {
    return mensagem;
  }

  public void setMensagem(String mensagem) {
    this.mensagem = mensagem;
  }

  public StatusNotificacao getStatus() {
    return status;
  }

  public void setStatus(StatusNotificacao status) {
    this.status = status;
  }

  public String getSourceEventId() {
    return sourceEventId;
  }

  public void setSourceEventId(String sourceEventId) {
    this.sourceEventId = sourceEventId;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getSentAt() {
    return sentAt;
  }

  public void setSentAt(Instant sentAt) {
    this.sentAt = sentAt;
  }

  public Instant getConfirmedAt() {
    return confirmedAt;
  }

  public void setConfirmedAt(Instant confirmedAt) {
    this.confirmedAt = confirmedAt;
  }

  public Instant getFailedAt() {
    return failedAt;
  }

  public void setFailedAt(Instant failedAt) {
    this.failedAt = failedAt;
  }

  public String getFailureReason() {
    return failureReason;
  }

  public void setFailureReason(String failureReason) {
    this.failureReason = failureReason;
  }
}

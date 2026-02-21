package br.com.condominio.servico.encomenda.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "outbox_event")
public class OutboxEventEntity {

  @Id
  @Column(name = "id", nullable = false, length = 36)
  private String id;

  @Column(name = "aggregatetype", nullable = false, length = 100)
  private String aggregateType;

  @Column(name = "aggregateid", nullable = false, length = 100)
  private String aggregateId;

  @Column(name = "type", nullable = false, length = 120)
  private String type;

  @Column(name = "event_version", nullable = false)
  private Integer eventVersion;

  @Lob
  @Column(name = "payload", nullable = false)
  private String payload;

  @Column(name = "event_timestamp", nullable = false)
  private Instant eventTimestamp;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public void setAggregateType(String aggregateType) {
    this.aggregateType = aggregateType;
  }

  public String getAggregateId() {
    return aggregateId;
  }

  public void setAggregateId(String aggregateId) {
    this.aggregateId = aggregateId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getEventVersion() {
    return eventVersion;
  }

  public void setEventVersion(Integer eventVersion) {
    this.eventVersion = eventVersion;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public Instant getEventTimestamp() {
    return eventTimestamp;
  }

  public void setEventTimestamp(Instant eventTimestamp) {
    this.eventTimestamp = eventTimestamp;
  }
}

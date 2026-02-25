CREATE TABLE notificacoes (
  id VARCHAR(36) PRIMARY KEY,
  encomenda_id VARCHAR(64) NOT NULL,
  morador_id VARCHAR(64) NOT NULL,
  canal VARCHAR(20) NOT NULL,
  destino VARCHAR(255) NOT NULL,
  mensagem VARCHAR(1000) NOT NULL,
  status VARCHAR(20) NOT NULL,
  source_event_id VARCHAR(64) NOT NULL UNIQUE,
  correlation_id VARCHAR(64) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  sent_at TIMESTAMP WITH TIME ZONE,
  confirmed_at TIMESTAMP WITH TIME ZONE,
  failed_at TIMESTAMP WITH TIME ZONE,
  failure_reason VARCHAR(500)
);

CREATE INDEX idx_notificacoes_morador_status_created_at ON notificacoes(morador_id, status, created_at);
CREATE INDEX idx_notificacoes_encomenda_morador ON notificacoes(encomenda_id, morador_id);

CREATE TABLE outbox_event (
  id VARCHAR(36) PRIMARY KEY,
  aggregatetype VARCHAR(100) NOT NULL,
  aggregateid VARCHAR(100) NOT NULL,
  type VARCHAR(120) NOT NULL,
  event_version INTEGER NOT NULL,
  payload TEXT NOT NULL,
  event_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
  event_timestamp_ms BIGINT NOT NULL
);

CREATE INDEX idx_outbox_aggregate ON outbox_event(aggregatetype, aggregateid);
CREATE INDEX idx_outbox_timestamp ON outbox_event(event_timestamp);
CREATE INDEX idx_outbox_timestamp_ms ON outbox_event(event_timestamp_ms);

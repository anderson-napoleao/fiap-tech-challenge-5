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

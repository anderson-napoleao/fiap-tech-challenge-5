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

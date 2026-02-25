ALTER TABLE outbox_event ADD COLUMN IF NOT EXISTS event_timestamp_ms BIGINT;

UPDATE outbox_event
SET event_timestamp_ms = CAST(EXTRACT(EPOCH FROM event_timestamp) * 1000 AS BIGINT)
WHERE event_timestamp_ms IS NULL;

ALTER TABLE outbox_event ALTER COLUMN event_timestamp_ms SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_timestamp_ms ON outbox_event(event_timestamp_ms);

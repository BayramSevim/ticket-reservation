CREATE TABLE outbox_events (
                               id           UUID PRIMARY KEY,
                               topic        VARCHAR(100) NOT NULL,
                               message_key  VARCHAR(100) NOT NULL,
                               payload      TEXT NOT NULL,
                               created_at   TIMESTAMPTZ NOT NULL,
                               published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox_events (created_at) WHERE published_at IS NULL;
ALTER TABLE alerts
    ADD COLUMN email_sent_at TIMESTAMP;

CREATE INDEX idx_alerts_email_pending
    ON alerts (email_sent_at, sent, type);
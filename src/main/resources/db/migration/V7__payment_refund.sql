ALTER TABLE payment ADD COLUMN refunded_at   TIMESTAMPTZ;
ALTER TABLE payment ADD COLUMN refund_reason VARCHAR(200);

ALTER TABLE payment
    ADD CONSTRAINT payment_refund_reason_required
    CHECK (refunded_at IS NULL OR refund_reason IS NOT NULL);

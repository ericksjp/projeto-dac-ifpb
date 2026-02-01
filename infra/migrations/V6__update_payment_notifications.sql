ALTER TABLE payment_notifications
DROP COLUMN payload;

ALTER TABLE payment_notifications
ADD COLUMN charge_external_id VARCHAR(100);

CREATE INDEX idx_payment_notifications_charge_external_id
ON payment_notifications (charge_external_id);

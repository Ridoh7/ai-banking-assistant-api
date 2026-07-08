ALTER TABLE refresh_tokens
ADD COLUMN browser VARCHAR(100);

ALTER TABLE refresh_tokens
ADD COLUMN operating_system VARCHAR(100);
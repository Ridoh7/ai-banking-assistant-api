ALTER TABLE refresh_tokens
ADD COLUMN session_id UUID;

UPDATE refresh_tokens
SET session_id = gen_random_uuid();

ALTER TABLE refresh_tokens
ALTER COLUMN session_id SET NOT NULL;
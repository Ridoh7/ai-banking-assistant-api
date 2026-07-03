-- Initialize optimistic locking version for existing accounts.

UPDATE accounts
SET version = 0
WHERE version IS NULL;
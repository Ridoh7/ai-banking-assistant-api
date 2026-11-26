-- =============================================================================
-- V14__align_audit_actor_id_with_users.sql
--
-- Aligns audit_logs.actor_id with users.id.
--
-- The users table uses BIGINT identity keys, while V13 initially declared
-- audit_logs.actor_id as UUID. Audit event persistence has not yet been
-- implemented, so no valid actor identifiers should exist at this stage.
-- This migration fails safely instead of silently discarding unexpected data.
-- =============================================================================

DO
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM audit_logs
        WHERE actor_id IS NOT NULL
    ) THEN
        RAISE EXCEPTION
            'Cannot convert audit_logs.actor_id to BIGINT because non-null actor IDs already exist.'
            USING
                ERRCODE = '55000',
                HINT = 'Investigate and migrate existing audit actor identifiers before rerunning Flyway.';
    END IF;
END;
$$;

ALTER TABLE audit_logs
    ALTER COLUMN actor_id TYPE BIGINT
    USING NULL::BIGINT;

COMMENT ON COLUMN audit_logs.actor_id IS
    'Internal BIGINT identifier of the authenticated user actor where available.';
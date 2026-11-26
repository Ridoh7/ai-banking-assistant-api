-- =============================================================================
-- V13__create_enterprise_audit_logs.sql
--
-- Creates the immutable enterprise audit log infrastructure.
--
-- Audit logs provide a permanent record of important authentication,
-- authorization, session, account, transaction, administration, security,
-- and system activities.
--
-- Audit records are append-only:
--     INSERT  -> Allowed
--     SELECT  -> Allowed
--     UPDATE  -> Rejected by database trigger
--     DELETE  -> Rejected by database trigger
-- =============================================================================


-- =============================================================================
-- 1. CREATE AUDIT LOGS TABLE
-- =============================================================================

CREATE TABLE audit_logs
(
    id                          UUID            NOT NULL,

    -- Event classification
    event_type                  VARCHAR(100)    NOT NULL,
    event_category              VARCHAR(50)     NOT NULL,
    event_outcome               VARCHAR(30)     NOT NULL,
    event_version               INTEGER         NOT NULL DEFAULT 1,

    -- Actor snapshot
    actor_id                    UUID,
    actor_type                  VARCHAR(30)     NOT NULL,
    actor_identifier            VARCHAR(255),
    actor_role                  VARCHAR(50),

    -- Primary affected resource
    resource_type               VARCHAR(50),
    resource_id                 VARCHAR(100),
    resource_name               VARCHAR(255),

    -- Optional secondary affected resource
    secondary_resource_type     VARCHAR(50),
    secondary_resource_id       VARCHAR(100),

    -- Request, correlation, and session context
    correlation_id              UUID            NOT NULL,
    request_id                  UUID,
    session_id                  UUID,
    ip_address                  VARCHAR(45),
    user_agent                  TEXT,
    device_type                 VARCHAR(50),
    device_name                 VARCHAR(150),
    browser                     VARCHAR(100),
    operating_system            VARCHAR(100),
    http_method                 VARCHAR(10),
    request_path                VARCHAR(500),

    -- Human-readable event information
    action                      VARCHAR(150)    NOT NULL,
    description                 VARCHAR(1000),
    failure_code                VARCHAR(100),
    failure_reason              VARCHAR(500),

    -- Sanitized event-specific information
    metadata                    JSONB,
    previous_state              JSONB,
    new_state                   JSONB,

    -- Record integrity and timing
    integrity_hash              VARCHAR(64)     NOT NULL,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_audit_logs
        PRIMARY KEY (id),

    CONSTRAINT chk_audit_logs_event_version_positive
        CHECK (event_version > 0),

    CONSTRAINT chk_audit_logs_event_type_not_blank
        CHECK (BTRIM(event_type) <> ''),

    CONSTRAINT chk_audit_logs_event_category_not_blank
        CHECK (BTRIM(event_category) <> ''),

    CONSTRAINT chk_audit_logs_event_outcome_not_blank
        CHECK (BTRIM(event_outcome) <> ''),

    CONSTRAINT chk_audit_logs_actor_type_not_blank
        CHECK (BTRIM(actor_type) <> ''),

    CONSTRAINT chk_audit_logs_action_not_blank
        CHECK (BTRIM(action) <> ''),

    CONSTRAINT chk_audit_logs_integrity_hash_format
        CHECK (integrity_hash ~ '^[A-Fa-f0-9]{64}$'),

    CONSTRAINT chk_audit_logs_http_method
        CHECK (
            http_method IS NULL
            OR http_method IN (
                'GET',
                'POST',
                'PUT',
                'PATCH',
                'DELETE',
                'OPTIONS',
                'HEAD'
            )
        )
);


-- =============================================================================
-- 2. TABLE AND COLUMN DOCUMENTATION
-- =============================================================================

COMMENT ON TABLE audit_logs IS
    'Immutable audit trail for security-sensitive, administrative, banking, transaction, and session activities.';

COMMENT ON COLUMN audit_logs.id IS
    'Application-generated unique identifier for the audit record.';

COMMENT ON COLUMN audit_logs.event_type IS
    'Specific stable event name, such as TRANSFER_SUCCESS or LOGIN_FAILURE.';

COMMENT ON COLUMN audit_logs.event_category IS
    'High-level category such as AUTHENTICATION, SESSION, TRANSACTION, or SECURITY.';

COMMENT ON COLUMN audit_logs.event_outcome IS
    'Final result of the operation: SUCCESS, FAILURE, or DENIED.';

COMMENT ON COLUMN audit_logs.event_version IS
    'Version of the audit event structure for future event-schema evolution.';

COMMENT ON COLUMN audit_logs.actor_id IS
    'Internal UUID of the authenticated actor where available.';

COMMENT ON COLUMN audit_logs.actor_type IS
    'Actor classification such as USER, ADMIN, SYSTEM, ANONYMOUS, or SERVICE.';

COMMENT ON COLUMN audit_logs.actor_identifier IS
    'Sanitized or masked actor identifier captured at the time of the event.';

COMMENT ON COLUMN audit_logs.actor_role IS
    'Actor role captured at the time of the audited operation.';

COMMENT ON COLUMN audit_logs.resource_type IS
    'Type of the primary resource affected by the operation.';

COMMENT ON COLUMN audit_logs.resource_id IS
    'Generic primary resource identifier. It may contain a UUID, reference, or other stable identifier.';

COMMENT ON COLUMN audit_logs.secondary_resource_type IS
    'Type of an optional secondary resource affected by the operation.';

COMMENT ON COLUMN audit_logs.secondary_resource_id IS
    'Identifier of an optional secondary affected resource.';

COMMENT ON COLUMN audit_logs.correlation_id IS
    'Identifier linking an audit record to application logs and related operations.';

COMMENT ON COLUMN audit_logs.request_id IS
    'Identifier for a specific HTTP request where available.';

COMMENT ON COLUMN audit_logs.session_id IS
    'Session identifier associated with the action where applicable.';

COMMENT ON COLUMN audit_logs.ip_address IS
    'IPv4 or IPv6 address associated with the request.';

COMMENT ON COLUMN audit_logs.user_agent IS
    'Original sanitized HTTP User-Agent value.';

COMMENT ON COLUMN audit_logs.metadata IS
    'Allowlisted and sanitized event-specific information stored as JSONB.';

COMMENT ON COLUMN audit_logs.previous_state IS
    'Allowlisted state values captured before an audited change.';

COMMENT ON COLUMN audit_logs.new_state IS
    'Allowlisted state values captured after an audited change.';

COMMENT ON COLUMN audit_logs.integrity_hash IS
    'SHA-256 hexadecimal hash calculated from immutable audit-event fields.';

COMMENT ON COLUMN audit_logs.created_at IS
    'Timezone-aware timestamp at which the audit record was created.';


-- =============================================================================
-- 3. QUERY PERFORMANCE INDEXES
-- =============================================================================

-- Supports default newest-first audit retrieval.
CREATE INDEX idx_audit_logs_created_at
    ON audit_logs (created_at DESC);


-- Supports filtering by a specific event type over time.
CREATE INDEX idx_audit_logs_event_type_created_at
    ON audit_logs (event_type, created_at DESC);


-- Supports category-based investigation over time.
CREATE INDEX idx_audit_logs_category_created_at
    ON audit_logs (event_category, created_at DESC);


-- Supports investigation of activity performed by one actor.
CREATE INDEX idx_audit_logs_actor_id_created_at
    ON audit_logs (actor_id, created_at DESC)
    WHERE actor_id IS NOT NULL;


-- Supports investigation of activity affecting a particular resource.
CREATE INDEX idx_audit_logs_resource
    ON audit_logs (resource_type, resource_id)
    WHERE resource_type IS NOT NULL
      AND resource_id IS NOT NULL;


-- Supports investigation by event result.
CREATE INDEX idx_audit_logs_outcome_created_at
    ON audit_logs (event_outcome, created_at DESC);


-- Supports investigation of all actions performed through one session.
CREATE INDEX idx_audit_logs_session_id
    ON audit_logs (session_id)
    WHERE session_id IS NOT NULL;


-- Supports tracing an operation using its correlation identifier.
CREATE INDEX idx_audit_logs_correlation_id
    ON audit_logs (correlation_id);


-- Supports security investigations involving an IP address.
CREATE INDEX idx_audit_logs_ip_address_created_at
    ON audit_logs (ip_address, created_at DESC)
    WHERE ip_address IS NOT NULL;


-- Supports administrative actor-type searches.
CREATE INDEX idx_audit_logs_actor_type_created_at
    ON audit_logs (actor_type, created_at DESC);


-- Supports searches involving the requested API endpoint.
CREATE INDEX idx_audit_logs_request_path_created_at
    ON audit_logs (request_path, created_at DESC)
    WHERE request_path IS NOT NULL;


-- =============================================================================
-- 4. DATABASE-LEVEL IMMUTABILITY
-- =============================================================================

CREATE OR REPLACE FUNCTION prevent_audit_log_modification()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
    RAISE EXCEPTION
        'Audit records are immutable. Operation % is not permitted on audit_logs.',
        TG_OP
        USING
            ERRCODE = '55000',
            HINT = 'Create a new audit record instead of modifying or deleting an existing record.';
END;
$$;


CREATE TRIGGER trg_prevent_audit_log_modification
    BEFORE UPDATE OR DELETE
    ON audit_logs
    FOR EACH ROW
EXECUTE FUNCTION prevent_audit_log_modification();


COMMENT ON FUNCTION prevent_audit_log_modification() IS
    'Rejects UPDATE and DELETE operations to enforce append-only audit storage.';


-- =============================================================================
-- END OF MIGRATION
-- =============================================================================
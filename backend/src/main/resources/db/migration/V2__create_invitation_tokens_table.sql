
-- ==========================================
-- V2: Create Invitation Tokens
-- ==========================================

CREATE TABLE invitation_tokens (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    role_id UUID NOT NULL,
    created_by UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_invitation_tokens_hash
        UNIQUE (token_hash),

    CONSTRAINT ck_invitation_tokens_expiration
        CHECK (expires_at > created_at),

    CONSTRAINT ck_invitation_tokens_usage
        CHECK (used_at IS NULL OR revoked_at IS NULL),

    CONSTRAINT fk_invitation_tokens_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id),

    CONSTRAINT fk_invitation_tokens_role
        FOREIGN KEY (role_id)
        REFERENCES roles (id),

    CONSTRAINT fk_invitation_tokens_creator
        FOREIGN KEY (created_by)
        REFERENCES users (id)
);

CREATE INDEX idx_invitation_tokens_tenant_id
    ON invitation_tokens (tenant_id);

CREATE INDEX idx_invitation_tokens_creator
    ON invitation_tokens (created_by);

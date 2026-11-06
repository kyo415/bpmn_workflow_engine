CREATE TABLE workflows (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_workflows_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT uk_workflows_tenant_code
        UNIQUE (tenant_id, code),

    CONSTRAINT ck_workflows_status
        CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'))
);

CREATE TABLE workflow_states (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(20) NOT NULL,

    CONSTRAINT fk_workflow_states_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES workflows(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_workflow_states_workflow_code
        UNIQUE (workflow_id, code),

    CONSTRAINT ck_workflow_states_type
        CHECK (type IN ('INITIAL', 'NORMAL', 'FINAL'))
);

CREATE TABLE workflow_steps (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    from_state_id UUID NOT NULL,
    to_state_id UUID NOT NULL,

    CONSTRAINT fk_workflow_steps_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES workflows(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_workflow_steps_from_state
        FOREIGN KEY (from_state_id)
        REFERENCES workflow_states(id),

    CONSTRAINT fk_workflow_steps_to_state
        FOREIGN KEY (to_state_id)
        REFERENCES workflow_states(id),

    CONSTRAINT uk_workflow_steps_workflow_code
        UNIQUE (workflow_id, code)
);
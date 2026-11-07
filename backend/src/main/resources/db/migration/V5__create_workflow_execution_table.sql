CREATE TABLE workflow_executions (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    current_state_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,

    CONSTRAINT fk_workflow_executions_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES workflows(id),

    CONSTRAINT fk_workflow_executions_current_state
        FOREIGN KEY (current_state_id)
        REFERENCES workflow_states(id),

    CONSTRAINT ck_workflow_executions_status
        CHECK (status IN ('RUNNING', 'COMPLETED', 'CANCELLED'))
);
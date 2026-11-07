package com.bpmn.workflowengine.workflow.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_executions")
public class WorkflowExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "current_state_id", nullable = false)
    private WorkflowState currentState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkflowExecutionStatus status;

    @Column(nullable = false)
    private Instant startedAt;

    @Column
    private Instant completedAt;

    protected WorkflowExecution() {
    }

    public WorkflowExecution(
            Workflow workflow,
            WorkflowState initialState
    ) {
        this.workflow = workflow;
        this.currentState = initialState;
        this.status = WorkflowExecutionStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public WorkflowState getCurrentState() {
        return currentState;
    }

    public WorkflowExecutionStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void transitionTo(WorkflowState nextState) {
        this.currentState = nextState;
    }

    public void complete() {
        this.status = WorkflowExecutionStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void cancel() {
        this.status = WorkflowExecutionStatus.CANCELLED;
        this.completedAt = Instant.now();
    }
}

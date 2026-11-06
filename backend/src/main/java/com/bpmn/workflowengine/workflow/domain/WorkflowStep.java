package com.bpmn.workflowengine.workflow.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "workflow_steps",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_workflow_steps_workflow_code",
                        columnNames = {"workflow_id", "code"}
                )
        }
)
public class WorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_state_id", nullable = false)
    private WorkflowState fromState;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_state_id", nullable = false)
    private WorkflowState toState;

    protected WorkflowStep() {
    }

    public WorkflowStep(
            Workflow workflow,
            String code,
            String name,
            WorkflowState fromState,
            WorkflowState toState
    ) {
        this.workflow = workflow;
        this.code = code;
        this.name = name;
        this.fromState = fromState;
        this.toState = toState;
    }

    public UUID getId() {
        return id;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public WorkflowState getFromState() {
        return fromState;
    }

    public WorkflowState getToState() {
        return toState;
    }
}

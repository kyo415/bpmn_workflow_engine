package com.bpmn.workflowengine.workflow.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "workflow_states",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_workflow_states_workflow_code",
                        columnNames = {"workflow_id", "code"}
                )
        }
)
public class WorkflowState {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkflowStateType type;

    protected WorkflowState() {
    }

    public WorkflowState(
            Workflow workflow,
            String code,
            String name,
            WorkflowStateType type
    ) {
        this.workflow = workflow;
        this.code = code;
        this.name = name;
        this.type = type;
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

    public WorkflowStateType getType() {
        return type;
    }
}

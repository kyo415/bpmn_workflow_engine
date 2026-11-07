package com.bpmn.workflowengine.workflow.service;

import com.bpmn.workflowengine.workflow.domain.Workflow;
import com.bpmn.workflowengine.workflow.domain.WorkflowExecution;
import com.bpmn.workflowengine.workflow.domain.WorkflowState;
import com.bpmn.workflowengine.workflow.domain.WorkflowStateType;
import com.bpmn.workflowengine.workflow.domain.WorkflowStep;
import com.bpmn.workflowengine.workflow.domain.WorkflowStatus;
import com.bpmn.workflowengine.workflow.repository.WorkflowExecutionRepository;
import com.bpmn.workflowengine.workflow.repository.WorkflowRepository;
import com.bpmn.workflowengine.workflow.repository.WorkflowStateRepository;
import com.bpmn.workflowengine.workflow.repository.WorkflowStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class WorkflowExecutionService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStateRepository workflowStateRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;

    public WorkflowExecutionService(
            WorkflowRepository workflowRepository,
            WorkflowStateRepository workflowStateRepository,
            WorkflowStepRepository workflowStepRepository,
            WorkflowExecutionRepository workflowExecutionRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowStateRepository = workflowStateRepository;
        this.workflowStepRepository = workflowStepRepository;
        this.workflowExecutionRepository = workflowExecutionRepository;
    }

    public WorkflowExecution startExecution(
            UUID tenantId,
            String workflowCode
    ) {
        Workflow workflow = workflowRepository
                .findByTenantIdAndCode(tenantId, workflowCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workflow not found"
                ));

        if (workflow.getStatus() != WorkflowStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Workflow must be ACTIVE to start execution"
            );
        }

        WorkflowState initialState = workflowStateRepository
                .findByWorkflowIdAndType(
                        workflow.getId(),
                        WorkflowStateType.INITIAL
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Initial state not found"
                ));

        WorkflowExecution execution = new WorkflowExecution(
                workflow,
                initialState
        );

        return workflowExecutionRepository.save(execution);
    }

    public WorkflowExecution transition(
            UUID executionId,
            String stepCode
    ) {
        WorkflowExecution execution = workflowExecutionRepository
                .findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workflow execution not found"
                ));

        if (execution.getStatus()
                != com.bpmn.workflowengine.workflow.domain.WorkflowExecutionStatus.RUNNING) {
            throw new IllegalArgumentException(
                    "Workflow execution is not RUNNING"
            );
        }

        WorkflowStep step = workflowStepRepository
                .findByWorkflowIdAndCode(
                        execution.getWorkflow().getId(),
                        stepCode
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workflow step not found"
                ));

        if (!execution.getCurrentState().getId()
                .equals(step.getFromState().getId())) {
            throw new IllegalArgumentException(
                    "Workflow step does not match current state"
            );
        }

        WorkflowState nextState = step.getToState();

        execution.transitionTo(nextState);

        if (nextState.getType() == WorkflowStateType.FINAL) {
            execution.complete();
        }

        return execution;
    }
}

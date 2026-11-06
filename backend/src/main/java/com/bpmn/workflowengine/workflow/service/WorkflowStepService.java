package com.bpmn.workflowengine.workflow.service;

import com.bpmn.workflowengine.workflow.domain.Workflow;
import com.bpmn.workflowengine.workflow.domain.WorkflowState;
import com.bpmn.workflowengine.workflow.domain.WorkflowStep;
import com.bpmn.workflowengine.workflow.repository.WorkflowRepository;
import com.bpmn.workflowengine.workflow.repository.WorkflowStateRepository;
import com.bpmn.workflowengine.workflow.repository.WorkflowStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class WorkflowStepService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStateRepository workflowStateRepository;
    private final WorkflowStepRepository workflowStepRepository;

    public WorkflowStepService(
            WorkflowRepository workflowRepository,
            WorkflowStateRepository workflowStateRepository,
            WorkflowStepRepository workflowStepRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowStateRepository = workflowStateRepository;
        this.workflowStepRepository = workflowStepRepository;
    }

    public WorkflowStep createStep(
            UUID workflowId,
            String code,
            String name,
            String fromStateCode,
            String toStateCode
    ) {
        if (workflowStepRepository.existsByWorkflowIdAndCode(
                workflowId,
                code
        )) {
            throw new IllegalArgumentException(
                    "Workflow step code already exists"
            );
        }

        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workflow not found"
                ));

        WorkflowState fromState = workflowStateRepository
                .findByWorkflowIdAndCode(workflowId, fromStateCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "From state not found"
                ));

        WorkflowState toState = workflowStateRepository
                .findByWorkflowIdAndCode(workflowId, toStateCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "To state not found"
                ));

        WorkflowStep step = new WorkflowStep(
                workflow,
                code,
                name,
                fromState,
                toState
        );

        return workflowStepRepository.save(step);
    }

    @Transactional(readOnly = true)
    public WorkflowStep getStep(
            UUID workflowId,
            String code
    ) {
        return workflowStepRepository
                .findByWorkflowIdAndCode(workflowId, code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workflow step not found"
                ));
    }
}
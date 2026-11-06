package com.bpmn.workflowengine.workflow.service;

import com.bpmn.workflowengine.workflow.domain.Workflow;
import com.bpmn.workflowengine.workflow.domain.WorkflowState;
import com.bpmn.workflowengine.workflow.domain.WorkflowStateType;
import com.bpmn.workflowengine.workflow.repository.WorkflowRepository;
import com.bpmn.workflowengine.workflow.repository.WorkflowStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class WorkflowStateService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStateRepository workflowStateRepository;

    public WorkflowStateService(
            WorkflowRepository workflowRepository,
            WorkflowStateRepository workflowStateRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowStateRepository = workflowStateRepository;
    }

    public WorkflowState createState(
            UUID workflowId,
            String code,
            String name,
            WorkflowStateType type
    ) {
        if (workflowStateRepository.existsByWorkflowIdAndCode(
                workflowId,
                code
        )) {
            throw new IllegalArgumentException(
                    "Workflow state code already exists"
            );
        }

        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workflow not found"
                ));

        WorkflowState state = new WorkflowState(
                workflow,
                code,
                name,
                type
        );

        return workflowStateRepository.save(state);
    }

    @Transactional(readOnly = true)
    public WorkflowState getState(
            UUID workflowId,
            String code
    ) {
        return workflowStateRepository
                .findByWorkflowIdAndCode(workflowId, code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workflow state not found"
                ));
    }
}

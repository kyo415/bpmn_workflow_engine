package com.bpmn.workflowengine.workflow.repository;

import com.bpmn.workflowengine.workflow.domain.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {

    Optional<WorkflowStep> findByWorkflowIdAndCode(
            UUID workflowId,
            String code
    );

    boolean existsByWorkflowIdAndCode(
            UUID workflowId,
            String code
    );
}

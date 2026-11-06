package com.bpmn.workflowengine.workflow.repository;

import com.bpmn.workflowengine.workflow.domain.WorkflowState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkflowStateRepository extends JpaRepository<WorkflowState, UUID> {

    Optional<WorkflowState> findByWorkflowIdAndCode(UUID workflowId, String code);

    boolean existsByWorkflowIdAndCode(UUID workflowId, String code);
}

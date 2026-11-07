package com.bpmn.workflowengine.workflow.repository;

import com.bpmn.workflowengine.workflow.domain.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkflowExecutionRepository
        extends JpaRepository<WorkflowExecution, UUID> {
}

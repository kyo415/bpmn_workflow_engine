package com.bpmn.workflowengine.workflow.repository;

import com.bpmn.workflowengine.workflow.domain.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {

    Optional<Workflow> findByTenantIdAndCode(UUID tenantId, String code);

    boolean existsByTenantIdAndCode(UUID tenantId, String code);
}

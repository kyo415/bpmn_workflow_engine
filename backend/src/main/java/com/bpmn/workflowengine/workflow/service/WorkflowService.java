package com.bpmn.workflowengine.workflow.service;

import com.bpmn.workflowengine.tenancy.domain.Tenant;
import com.bpmn.workflowengine.tenancy.persistence.TenantRepository;
import com.bpmn.workflowengine.workflow.domain.Workflow;
import com.bpmn.workflowengine.workflow.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final TenantRepository tenantRepository;

    public WorkflowService(
            WorkflowRepository workflowRepository,
            TenantRepository tenantRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.tenantRepository = tenantRepository;
    }

    public Workflow createWorkflow(
            UUID tenantId,
            String code,
            String name,
            String description
    ) {
        if (workflowRepository.existsByTenantIdAndCode(tenantId, code)) {
            throw new IllegalArgumentException(
                    "Workflow code already exists for this tenant"
            );
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tenant not found"
                ));

        Workflow workflow = new Workflow(
                tenant,
                code,
                name,
                description
        );

        return workflowRepository.save(workflow);
    }

    @Transactional(readOnly = true)
    public Workflow getWorkflow(UUID tenantId, String code) {
        return workflowRepository
                .findByTenantIdAndCode(tenantId, code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workflow not found"
                ));
    }

    public void activateWorkflow(UUID tenantId, String code) {
        Workflow workflow = getWorkflow(tenantId, code);
        workflow.activate();
    }

    public void deactivateWorkflow(UUID tenantId, String code) {
        Workflow workflow = getWorkflow(tenantId, code);
        workflow.deactivate();
    }
}

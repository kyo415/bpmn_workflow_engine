package com.bpmn.workflowengine.workflow.service;

import com.bpmn.workflowengine.tenancy.domain.Tenant;
import com.bpmn.workflowengine.tenancy.persistence.TenantRepository;
import com.bpmn.workflowengine.workflow.domain.Workflow;
import com.bpmn.workflowengine.workflow.repository.WorkflowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private WorkflowService workflowService;

    @Test
    void shouldCreateWorkflowWhenTenantExists() {
        UUID tenantId = UUID.randomUUID();

        Tenant tenant = mock(Tenant.class);

        when(workflowRepository.existsByTenantIdAndCode(
                tenantId,
                "RECEIVING"
        )).thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        Workflow savedWorkflow = mock(Workflow.class);

        when(workflowRepository.save(any(Workflow.class)))
                .thenReturn(savedWorkflow);

        Workflow result = workflowService.createWorkflow(
                tenantId,
                "RECEIVING",
                "Quy trình tiếp nhận hàng",
                "Quy trình tiếp nhận hàng hóa"
        );

        assertSame(savedWorkflow, result);

        verify(tenantRepository).findById(tenantId);
        verify(workflowRepository).save(any(Workflow.class));
    }

    @Test
    void shouldRejectDuplicateWorkflowCodeForSameTenant() {
        UUID tenantId = UUID.randomUUID();

        when(workflowRepository.existsByTenantIdAndCode(
                tenantId,
                "RECEIVING"
        )).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowService.createWorkflow(
                        tenantId,
                        "RECEIVING",
                        "Quy trình tiếp nhận hàng",
                        null
                )
        );

        assertEquals(
                "Workflow code already exists for this tenant",
                exception.getMessage()
        );

        verify(tenantRepository, never()).findById(any());
        verify(workflowRepository, never()).save(any());
    }

    @Test
    void shouldRejectWorkflowWhenTenantDoesNotExist() {
        UUID tenantId = UUID.randomUUID();

        when(workflowRepository.existsByTenantIdAndCode(
                tenantId,
                "RECEIVING"
        )).thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowService.createWorkflow(
                        tenantId,
                        "RECEIVING",
                        "Quy trình tiếp nhận hàng",
                        null
                )
        );

        assertEquals(
                "Tenant not found",
                exception.getMessage()
        );

        verify(workflowRepository, never()).save(any());
    }
	    @Test
    void shouldGetWorkflowByTenantAndCode() {
        UUID tenantId = UUID.randomUUID();
        Workflow workflow = mock(Workflow.class);

        when(workflowRepository.findByTenantIdAndCode(
                tenantId,
                "RECEIVING"
        )).thenReturn(Optional.of(workflow));

        Workflow result = workflowService.getWorkflow(
                tenantId,
                "RECEIVING"
        );

        assertSame(workflow, result);

        verify(workflowRepository)
                .findByTenantIdAndCode(tenantId, "RECEIVING");
    }

    @Test
    void shouldActivateWorkflow() {
        UUID tenantId = UUID.randomUUID();
        Workflow workflow = mock(Workflow.class);

        when(workflowRepository.findByTenantIdAndCode(
                tenantId,
                "RECEIVING"
        )).thenReturn(Optional.of(workflow));

        workflowService.activateWorkflow(
                tenantId,
                "RECEIVING"
        );

        verify(workflow).activate();
    }

    @Test
    void shouldDeactivateWorkflow() {
        UUID tenantId = UUID.randomUUID();
        Workflow workflow = mock(Workflow.class);

        when(workflowRepository.findByTenantIdAndCode(
                tenantId,
                "RECEIVING"
        )).thenReturn(Optional.of(workflow));

        workflowService.deactivateWorkflow(
                tenantId,
                "RECEIVING"
        );

        verify(workflow).deactivate();
    }
        @Test
    void shouldRejectGetWorkflowWhenWorkflowDoesNotExist() {
        UUID tenantId = UUID.randomUUID();

        when(workflowRepository.findByTenantIdAndCode(
                tenantId,
                "RECEIVING"
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowService.getWorkflow(
                        tenantId,
                        "RECEIVING"
                )
        );

        assertEquals(
                "Workflow not found",
                exception.getMessage()
        );
    }
}

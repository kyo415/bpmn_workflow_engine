package com.bpmn.workflowengine.workflow.service;

import com.bpmn.workflowengine.workflow.domain.Workflow;
import com.bpmn.workflowengine.workflow.domain.WorkflowState;
import com.bpmn.workflowengine.workflow.domain.WorkflowStep;
import com.bpmn.workflowengine.workflow.repository.WorkflowRepository;
import com.bpmn.workflowengine.workflow.repository.WorkflowStateRepository;
import com.bpmn.workflowengine.workflow.repository.WorkflowStepRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowStepServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowStateRepository workflowStateRepository;

    @Mock
    private WorkflowStepRepository workflowStepRepository;

    @InjectMocks
    private WorkflowStepService workflowStepService;

    @Test
    void shouldCreateStepWhenWorkflowAndStatesExist() {
        UUID workflowId = UUID.randomUUID();

        Workflow workflow = mock(Workflow.class);
        WorkflowState fromState = mock(WorkflowState.class);
        WorkflowState toState = mock(WorkflowState.class);
        WorkflowStep savedStep = mock(WorkflowStep.class);

        when(workflowStepRepository.existsByWorkflowIdAndCode(
                workflowId,
                "CLASSIFY"
        )).thenReturn(false);

        when(workflowRepository.findById(workflowId))
                .thenReturn(Optional.of(workflow));

        when(workflowStateRepository.findByWorkflowIdAndCode(
                workflowId,
                "RECEIVED"
        )).thenReturn(Optional.of(fromState));

        when(workflowStateRepository.findByWorkflowIdAndCode(
                workflowId,
                "CLASSIFIED"
        )).thenReturn(Optional.of(toState));

        when(workflowStepRepository.save(any(WorkflowStep.class)))
                .thenReturn(savedStep);

        WorkflowStep result = workflowStepService.createStep(
                workflowId,
                "CLASSIFY",
                "Phân loại hàng",
                "RECEIVED",
                "CLASSIFIED"
        );

        assertSame(savedStep, result);

        verify(workflowRepository).findById(workflowId);
        verify(workflowStateRepository)
                .findByWorkflowIdAndCode(workflowId, "RECEIVED");
        verify(workflowStateRepository)
                .findByWorkflowIdAndCode(workflowId, "CLASSIFIED");
        verify(workflowStepRepository).save(any(WorkflowStep.class));
    }

    @Test
    void shouldRejectDuplicateStepCodeForSameWorkflow() {
        UUID workflowId = UUID.randomUUID();

        when(workflowStepRepository.existsByWorkflowIdAndCode(
                workflowId,
                "CLASSIFY"
        )).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowStepService.createStep(
                        workflowId,
                        "CLASSIFY",
                        "Phân loại hàng",
                        "RECEIVED",
                        "CLASSIFIED"
                )
        );

        assertEquals(
                "Workflow step code already exists",
                exception.getMessage()
        );

        verify(workflowRepository, never()).findById(any());
        verify(workflowStateRepository, never())
                .findByWorkflowIdAndCode(any(), any());
        verify(workflowStepRepository, never()).save(any());
    }

    @Test
    void shouldRejectStepWhenWorkflowDoesNotExist() {
        UUID workflowId = UUID.randomUUID();

        when(workflowStepRepository.existsByWorkflowIdAndCode(
                workflowId,
                "CLASSIFY"
        )).thenReturn(false);

        when(workflowRepository.findById(workflowId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowStepService.createStep(
                        workflowId,
                        "CLASSIFY",
                        "Phân loại hàng",
                        "RECEIVED",
                        "CLASSIFIED"
                )
        );

        assertEquals(
                "Workflow not found",
                exception.getMessage()
        );

        verify(workflowStateRepository, never())
                .findByWorkflowIdAndCode(any(), any());
        verify(workflowStepRepository, never()).save(any());
    }

    @Test
    void shouldRejectStepWhenFromStateDoesNotExist() {
        UUID workflowId = UUID.randomUUID();

        Workflow workflow = mock(Workflow.class);

        when(workflowStepRepository.existsByWorkflowIdAndCode(
                workflowId,
                "CLASSIFY"
        )).thenReturn(false);

        when(workflowRepository.findById(workflowId))
                .thenReturn(Optional.of(workflow));

        when(workflowStateRepository.findByWorkflowIdAndCode(
                workflowId,
                "RECEIVED"
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowStepService.createStep(
                        workflowId,
                        "CLASSIFY",
                        "Phân loại hàng",
                        "RECEIVED",
                        "CLASSIFIED"
                )
        );

        assertEquals(
                "From state not found",
                exception.getMessage()
        );

        verify(workflowStateRepository, never())
                .findByWorkflowIdAndCode(workflowId, "CLASSIFIED");
        verify(workflowStepRepository, never()).save(any());
    }

    @Test
    void shouldRejectStepWhenToStateDoesNotExist() {
        UUID workflowId = UUID.randomUUID();

        Workflow workflow = mock(Workflow.class);
        WorkflowState fromState = mock(WorkflowState.class);

        when(workflowStepRepository.existsByWorkflowIdAndCode(
                workflowId,
                "CLASSIFY"
        )).thenReturn(false);

        when(workflowRepository.findById(workflowId))
                .thenReturn(Optional.of(workflow));

        when(workflowStateRepository.findByWorkflowIdAndCode(
                workflowId,
                "RECEIVED"
        )).thenReturn(Optional.of(fromState));

        when(workflowStateRepository.findByWorkflowIdAndCode(
                workflowId,
                "CLASSIFIED"
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowStepService.createStep(
                        workflowId,
                        "CLASSIFY",
                        "Phân loại hàng",
                        "RECEIVED",
                        "CLASSIFIED"
                )
        );

        assertEquals(
                "To state not found",
                exception.getMessage()
        );

        verify(workflowStepRepository, never()).save(any());
    }

    @Test
    void shouldGetStepByWorkflowAndCode() {
        UUID workflowId = UUID.randomUUID();
        WorkflowStep step = mock(WorkflowStep.class);

        when(workflowStepRepository.findByWorkflowIdAndCode(
                workflowId,
                "CLASSIFY"
        )).thenReturn(Optional.of(step));

        WorkflowStep result = workflowStepService.getStep(
                workflowId,
                "CLASSIFY"
        );

        assertSame(step, result);

        verify(workflowStepRepository)
                .findByWorkflowIdAndCode(workflowId, "CLASSIFY");
    }

    @Test
    void shouldRejectGetStepWhenStepDoesNotExist() {
        UUID workflowId = UUID.randomUUID();

        when(workflowStepRepository.findByWorkflowIdAndCode(
                workflowId,
                "CLASSIFY"
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowStepService.getStep(
                        workflowId,
                        "CLASSIFY"
                )
        );

        assertEquals(
                "Workflow step not found",
                exception.getMessage()
        );
    }
}
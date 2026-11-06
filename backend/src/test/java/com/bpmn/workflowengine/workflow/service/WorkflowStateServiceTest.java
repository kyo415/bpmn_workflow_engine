package com.bpmn.workflowengine.workflow.service;

import com.bpmn.workflowengine.workflow.domain.Workflow;
import com.bpmn.workflowengine.workflow.domain.WorkflowState;
import com.bpmn.workflowengine.workflow.domain.WorkflowStateType;
import com.bpmn.workflowengine.workflow.repository.WorkflowRepository;
import com.bpmn.workflowengine.workflow.repository.WorkflowStateRepository;
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
class WorkflowStateServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowStateRepository workflowStateRepository;

    @InjectMocks
    private WorkflowStateService workflowStateService;

    @Test
    void shouldCreateStateWhenWorkflowExists() {
        UUID workflowId = UUID.randomUUID();
        Workflow workflow = mock(Workflow.class);
        WorkflowState savedState = mock(WorkflowState.class);

        when(workflowStateRepository.existsByWorkflowIdAndCode(
                workflowId,
                "RECEIVED"
        )).thenReturn(false);

        when(workflowRepository.findById(workflowId))
                .thenReturn(Optional.of(workflow));

        when(workflowStateRepository.save(any(WorkflowState.class)))
                .thenReturn(savedState);

        WorkflowState result = workflowStateService.createState(
                workflowId,
                "RECEIVED",
                "Đã tiếp nhận",
                WorkflowStateType.INITIAL
        );

        assertSame(savedState, result);

        verify(workflowRepository).findById(workflowId);
        verify(workflowStateRepository).save(any(WorkflowState.class));
    }

    @Test
    void shouldRejectDuplicateStateCodeForSameWorkflow() {
        UUID workflowId = UUID.randomUUID();

        when(workflowStateRepository.existsByWorkflowIdAndCode(
                workflowId,
                "RECEIVED"
        )).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowStateService.createState(
                        workflowId,
                        "RECEIVED",
                        "Đã tiếp nhận",
                        WorkflowStateType.INITIAL
                )
        );

        assertEquals(
                "Workflow state code already exists",
                exception.getMessage()
        );

        verify(workflowRepository, never()).findById(any());
        verify(workflowStateRepository, never()).save(any());
    }

    @Test
    void shouldRejectStateWhenWorkflowDoesNotExist() {
        UUID workflowId = UUID.randomUUID();

        when(workflowStateRepository.existsByWorkflowIdAndCode(
                workflowId,
                "RECEIVED"
        )).thenReturn(false);

        when(workflowRepository.findById(workflowId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowStateService.createState(
                        workflowId,
                        "RECEIVED",
                        "Đã tiếp nhận",
                        WorkflowStateType.INITIAL
                )
        );

        assertEquals(
                "Workflow not found",
                exception.getMessage()
        );

        verify(workflowStateRepository, never()).save(any());
    }

    @Test
    void shouldGetStateByWorkflowAndCode() {
        UUID workflowId = UUID.randomUUID();
        WorkflowState state = mock(WorkflowState.class);

        when(workflowStateRepository.findByWorkflowIdAndCode(
                workflowId,
                "RECEIVED"
        )).thenReturn(Optional.of(state));

        WorkflowState result = workflowStateService.getState(
                workflowId,
                "RECEIVED"
        );

        assertSame(state, result);

        verify(workflowStateRepository)
                .findByWorkflowIdAndCode(workflowId, "RECEIVED");
    }

    @Test
    void shouldRejectGetStateWhenStateDoesNotExist() {
        UUID workflowId = UUID.randomUUID();

        when(workflowStateRepository.findByWorkflowIdAndCode(
                workflowId,
                "RECEIVED"
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowStateService.getState(
                        workflowId,
                        "RECEIVED"
                )
        );

        assertEquals(
                "Workflow state not found",
                exception.getMessage()
        );
    }
}

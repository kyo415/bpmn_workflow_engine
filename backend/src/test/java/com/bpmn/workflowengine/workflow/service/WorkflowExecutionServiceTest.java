package com.bpmn.workflowengine.workflow.service;

import com.bpmn.workflowengine.workflow.domain.*;
import com.bpmn.workflowengine.workflow.repository.WorkflowExecutionRepository;
import com.bpmn.workflowengine.workflow.repository.WorkflowRepository;
import com.bpmn.workflowengine.workflow.repository.WorkflowStateRepository;
import com.bpmn.workflowengine.workflow.repository.WorkflowStepRepository;
import org.junit.jupiter.api.BeforeEach;
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
class WorkflowExecutionServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowStateRepository workflowStateRepository;

    @Mock
    private WorkflowStepRepository workflowStepRepository;

    @Mock
    private WorkflowExecutionRepository workflowExecutionRepository;

    @InjectMocks
    private WorkflowExecutionService workflowExecutionService;

    private UUID tenantId;
    private UUID workflowId;
    private UUID executionId;
    private UUID currentStateId;
    private UUID nextStateId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        workflowId = UUID.randomUUID();
        executionId = UUID.randomUUID();
        currentStateId = UUID.randomUUID();
        nextStateId = UUID.randomUUID();
    }

    @Test
    void shouldStartExecutionWhenWorkflowIsActiveAndInitialStateExists() {
        String workflowCode = "GOODS_INBOUND";

        Workflow workflow = mock(Workflow.class);
        WorkflowState initialState = mock(WorkflowState.class);

        when(workflowRepository.findByTenantIdAndCode(
                tenantId,
                workflowCode
        )).thenReturn(Optional.of(workflow));

        when(workflow.getStatus())
                .thenReturn(WorkflowStatus.ACTIVE);

        when(workflow.getId())
                .thenReturn(workflowId);

        when(workflowStateRepository.findByWorkflowIdAndType(
                workflowId,
                WorkflowStateType.INITIAL
        )).thenReturn(Optional.of(initialState));

        when(workflowExecutionRepository.save(any(WorkflowExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowExecution result =
                workflowExecutionService.startExecution(
                        tenantId,
                        workflowCode
                );

        assertNotNull(result);
        assertSame(workflow, result.getWorkflow());
        assertSame(initialState, result.getCurrentState());
        assertEquals(
                WorkflowExecutionStatus.RUNNING,
                result.getStatus()
        );
        assertNotNull(result.getStartedAt());

        verify(workflowExecutionRepository)
                .save(any(WorkflowExecution.class));
    }

    @Test
    void shouldRejectWhenWorkflowDoesNotExist() {
        when(workflowRepository.findByTenantIdAndCode(
                tenantId,
                "GOODS_INBOUND"
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowExecutionService.startExecution(
                        tenantId,
                        "GOODS_INBOUND"
                )
        );

        assertEquals("Workflow not found", exception.getMessage());
        verifyNoInteractions(workflowStateRepository);
        verifyNoInteractions(workflowExecutionRepository);
    }

    @Test
    void shouldRejectWhenWorkflowIsNotActive() {
        Workflow workflow = mock(Workflow.class);

        when(workflowRepository.findByTenantIdAndCode(
                tenantId,
                "GOODS_INBOUND"
        )).thenReturn(Optional.of(workflow));

        when(workflow.getStatus())
                .thenReturn(WorkflowStatus.DRAFT);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowExecutionService.startExecution(
                        tenantId,
                        "GOODS_INBOUND"
                )
        );

        assertEquals(
                "Workflow must be ACTIVE to start execution",
                exception.getMessage()
        );

        verifyNoInteractions(workflowStateRepository);
        verifyNoInteractions(workflowExecutionRepository);
    }

    @Test
    void shouldRejectWhenInitialStateDoesNotExist() {
        Workflow workflow = mock(Workflow.class);

        when(workflowRepository.findByTenantIdAndCode(
                tenantId,
                "GOODS_INBOUND"
        )).thenReturn(Optional.of(workflow));

        when(workflow.getStatus())
                .thenReturn(WorkflowStatus.ACTIVE);

        when(workflow.getId())
                .thenReturn(workflowId);

        when(workflowStateRepository.findByWorkflowIdAndType(
                workflowId,
                WorkflowStateType.INITIAL
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowExecutionService.startExecution(
                        tenantId,
                        "GOODS_INBOUND"
                )
        );

        assertEquals(
                "Initial state not found",
                exception.getMessage()
        );

        verifyNoInteractions(workflowExecutionRepository);
    }

    @Test
    void shouldTransitionToNextState() {
        Workflow workflow = mock(Workflow.class);
        WorkflowState currentState = mock(WorkflowState.class);
        WorkflowState nextState = mock(WorkflowState.class);
        WorkflowStep step = mock(WorkflowStep.class);
        WorkflowExecution execution = mockExecution(
                workflow,
                currentState,
                WorkflowExecutionStatus.RUNNING
        );

        when(workflow.getId()).thenReturn(workflowId);
        when(currentState.getId()).thenReturn(currentStateId);
        when(nextState.getType()).thenReturn(WorkflowStateType.NORMAL);

        when(workflowExecutionRepository.findById(executionId))
                .thenReturn(Optional.of(execution));

        when(workflowStepRepository.findByWorkflowIdAndCode(
                workflowId,
                "CHECK"
        )).thenReturn(Optional.of(step));

        when(step.getFromState()).thenReturn(currentState);
        when(step.getToState()).thenReturn(nextState);

        WorkflowExecution result =
                workflowExecutionService.transition(
                        executionId,
                        "CHECK"
                );

        verify(execution).transitionTo(nextState);
        verify(execution, never()).complete();
        assertSame(execution, result);
    }

    @Test
    void shouldRejectWhenExecutionDoesNotExist() {
        when(workflowExecutionRepository.findById(executionId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowExecutionService.transition(
                        executionId,
                        "CHECK"
                )
        );

        assertEquals(
                "Workflow execution not found",
                exception.getMessage()
        );

        verifyNoInteractions(workflowStepRepository);
    }

    @Test
    void shouldRejectWhenExecutionIsNotRunning() {
        WorkflowExecution execution = mock(WorkflowExecution.class);

        when(workflowExecutionRepository.findById(executionId))
                .thenReturn(Optional.of(execution));

        when(execution.getStatus())
                .thenReturn(WorkflowExecutionStatus.COMPLETED);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowExecutionService.transition(
                        executionId,
                        "CHECK"
                )
        );

        assertEquals(
                "Workflow execution is not RUNNING",
                exception.getMessage()
        );

        verifyNoInteractions(workflowStepRepository);
    }

    @Test
    void shouldRejectWhenWorkflowStepDoesNotExist() {
        Workflow workflow = mock(Workflow.class);
        WorkflowState currentState = mock(WorkflowState.class);

        WorkflowExecution execution = mock(WorkflowExecution.class);

        when(execution.getWorkflow()).thenReturn(workflow);
        when(execution.getStatus())
                .thenReturn(WorkflowExecutionStatus.RUNNING);

        when(workflow.getId()).thenReturn(workflowId);

        when(workflowExecutionRepository.findById(executionId))
                .thenReturn(Optional.of(execution));

        when(workflowStepRepository.findByWorkflowIdAndCode(
                workflowId,
                "INVALID_STEP"
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowExecutionService.transition(
                        executionId,
                        "INVALID_STEP"
                )
        );

        assertEquals(
                "Workflow step not found",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectWhenStepDoesNotMatchCurrentState() {
        Workflow workflow = mock(Workflow.class);
        WorkflowState currentState = mock(WorkflowState.class);
        WorkflowState wrongFromState = mock(WorkflowState.class);
        WorkflowState nextState = mock(WorkflowState.class);
        WorkflowStep step = mock(WorkflowStep.class);

        WorkflowExecution execution = mockExecution(
                workflow,
                currentState,
                WorkflowExecutionStatus.RUNNING
        );

        when(workflow.getId()).thenReturn(workflowId);
        when(currentState.getId()).thenReturn(currentStateId);
        when(wrongFromState.getId()).thenReturn(UUID.randomUUID());

        when(workflowExecutionRepository.findById(executionId))
                .thenReturn(Optional.of(execution));

        when(workflowStepRepository.findByWorkflowIdAndCode(
                workflowId,
                "SKIP"
        )).thenReturn(Optional.of(step));

        when(step.getFromState()).thenReturn(wrongFromState);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workflowExecutionService.transition(
                        executionId,
                        "SKIP"
                )
        );

        assertEquals(
                "Workflow step does not match current state",
                exception.getMessage()
        );

        verify(execution, never()).transitionTo(any());
    }

    @Test
    void shouldCompleteWhenTransitionReachesFinalState() {
        Workflow workflow = mock(Workflow.class);
        WorkflowState currentState = mock(WorkflowState.class);
        WorkflowState finalState = mock(WorkflowState.class);
        WorkflowStep step = mock(WorkflowStep.class);

        WorkflowExecution execution = mockExecution(
                workflow,
                currentState,
                WorkflowExecutionStatus.RUNNING
        );

        when(workflow.getId()).thenReturn(workflowId);
        when(currentState.getId()).thenReturn(currentStateId);
        when(finalState.getType()).thenReturn(WorkflowStateType.FINAL);

        when(workflowExecutionRepository.findById(executionId))
                .thenReturn(Optional.of(execution));

        when(workflowStepRepository.findByWorkflowIdAndCode(
                workflowId,
                "COMPLETE"
        )).thenReturn(Optional.of(step));

        when(step.getFromState()).thenReturn(currentState);
        when(step.getToState()).thenReturn(finalState);

        WorkflowExecution result =
                workflowExecutionService.transition(
                        executionId,
                        "COMPLETE"
                );

        verify(execution).transitionTo(finalState);
        verify(execution).complete();
        assertSame(execution, result);
    }

    private WorkflowExecution mockExecution(
            Workflow workflow,
            WorkflowState currentState,
            WorkflowExecutionStatus status
    ) {
        WorkflowExecution execution = mock(WorkflowExecution.class);

        when(execution.getWorkflow()).thenReturn(workflow);
        when(execution.getCurrentState()).thenReturn(currentState);
        when(execution.getStatus()).thenReturn(status);

        return execution;
    }
}

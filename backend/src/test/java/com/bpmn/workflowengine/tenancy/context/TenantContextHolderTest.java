package com.bpmn.workflowengine.tenancy.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextHolderTest {

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void shouldSetAndGetTenantId() {
        UUID tenantId = UUID.randomUUID();

        TenantContextHolder.setTenantId(tenantId);

        assertEquals(tenantId, TenantContextHolder.getTenantId().orElse(null));
    }

    @Test
    void shouldThrowExceptionWhenTenantIdIsNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> TenantContextHolder.setTenantId(null)
        );
    }

    @Test
    void shouldClearTenantId() {
        UUID tenantId = UUID.randomUUID();
        TenantContextHolder.setTenantId(tenantId);

        TenantContextHolder.clear();

        assertTrue(TenantContextHolder.getTenantId().isEmpty());
    }
}
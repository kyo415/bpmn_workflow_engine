package com.bpmn.workflowengine.tenancy.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HeaderTenantResolverTest {

    private TenantResolver tenantResolver;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        tenantResolver = new HeaderTenantResolver();
        request = mock(HttpServletRequest.class);
    }

    @Test
    void shouldResolveTenantIdFromHeader() {
        UUID expectedTenantId = UUID.randomUUID();

        when(request.getHeader("X-Tenant-Id"))
                .thenReturn(expectedTenantId.toString());

        UUID actualTenantId = tenantResolver.resolveTenantId(request);

        assertEquals(expectedTenantId, actualTenantId);
    }

    @Test
    void shouldThrowExceptionWhenHeaderIsMissing() {
        when(request.getHeader("X-Tenant-Id"))
                .thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tenantResolver.resolveTenantId(request)
        );

        assertEquals("Missing tenant ID", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenHeaderIsInvalid() {
        when(request.getHeader("X-Tenant-Id"))
                .thenReturn("invalid-uuid");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tenantResolver.resolveTenantId(request)
        );

        assertEquals("Invalid tenant ID", exception.getMessage());
    }
}
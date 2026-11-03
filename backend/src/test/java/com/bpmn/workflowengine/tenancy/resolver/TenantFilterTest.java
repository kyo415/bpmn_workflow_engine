
package com.bpmn.workflowengine.tenancy.resolver;

import com.bpmn.workflowengine.tenancy.context.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantFilterTest {

    private TenantResolver tenantResolver;
    private TestableTenantFilter tenantFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        tenantResolver = mock(TenantResolver.class);
        tenantFilter = new TestableTenantFilter(tenantResolver);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        TenantContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void shouldSetTenantContextAndContinueFilterChain()
            throws ServletException, IOException {

        UUID tenantId = UUID.randomUUID();

        when(tenantResolver.resolveTenantId(request))
                .thenReturn(tenantId);

        doAnswer(invocation -> {
            assertEquals(
                    tenantId,
                    TenantContextHolder.getTenantId().orElse(null)
            );
            return null;
        }).when(filterChain).doFilter(request, response);

        tenantFilter.execute(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertTrue(TenantContextHolder.getTenantId().isEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenTenantIdIsMissing()
            throws ServletException, IOException {

        when(tenantResolver.resolveTenantId(request))
                .thenThrow(new IllegalArgumentException("Missing tenant ID"));

        tenantFilter.execute(request, response, filterChain);

        verify(response).sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Missing tenant ID"
        );

        verify(filterChain, never()).doFilter(request, response);

        assertTrue(TenantContextHolder.getTenantId().isEmpty());
    }

    @Test
    void shouldClearTenantContextWhenFilterChainThrowsException()
            throws ServletException, IOException {

        UUID tenantId = UUID.randomUUID();

        when(tenantResolver.resolveTenantId(request))
                .thenReturn(tenantId);

        doAnswer(invocation -> {
            assertEquals(
                    tenantId,
                    TenantContextHolder.getTenantId().orElse(null)
            );
            throw new ServletException("Test exception");
        }).when(filterChain).doFilter(request, response);

        assertThrows(
                ServletException.class,
                () -> tenantFilter.execute(request, response, filterChain)
        );

        assertTrue(TenantContextHolder.getTenantId().isEmpty());
    }

        @Test
        void shouldSkipHealthEndpoint() {
            when(request.getRequestURI()).thenReturn("/actuator/health");

            assertTrue(tenantFilter.shouldSkip(request));
        }

        @Test
        void shouldNotSkipBusinessEndpoint() {
            when(request.getRequestURI()).thenReturn("/api/shipments");

            assertFalse(tenantFilter.shouldSkip(request));
        }

    private static class TestableTenantFilter extends TenantFilter {

        TestableTenantFilter(TenantResolver tenantResolver) {
            super(tenantResolver);
        }

        void execute(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            super.doFilterInternal(request, response, filterChain);
        }

        boolean shouldSkip(HttpServletRequest request) {
            return super.shouldNotFilter(request);
        }
    }
}

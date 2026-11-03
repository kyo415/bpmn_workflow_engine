package com.bpmn.workflowengine.tenancy.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HeaderTenantResolver implements TenantResolver {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    public UUID resolveTenantId(HttpServletRequest request) {
        String tenantId = request.getHeader(TENANT_HEADER);

        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Missing tenant ID");
        }

        try {
            return UUID.fromString(tenantId);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid tenant ID", exception);
        }
    }
}
package com.bpmn.workflowengine.tenancy.resolver;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface TenantResolver {

    UUID resolveTenantId(HttpServletRequest request);
}
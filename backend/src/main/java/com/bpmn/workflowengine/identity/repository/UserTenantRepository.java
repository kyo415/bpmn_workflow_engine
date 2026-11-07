package com.bpmn.workflowengine.identity.repository;

import com.bpmn.workflowengine.identity.domain.MembershipStatus;
import com.bpmn.workflowengine.identity.domain.UserTenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserTenantRepository
        extends JpaRepository<UserTenant, UUID> {

    boolean existsByUser_IdAndTenant_Id(UUID userId, UUID tenantId);

    boolean existsByUser_IdAndTenant_IdAndStatusAndRole_Code(
            UUID userId,
            UUID tenantId,
            MembershipStatus status,
            String roleCode
    );
}
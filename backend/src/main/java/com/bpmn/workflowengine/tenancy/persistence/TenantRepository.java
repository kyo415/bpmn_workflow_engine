package com.bpmn.workflowengine.tenancy.persistence;

import com.bpmn.workflowengine.tenancy.domain.Tenant;
import com.bpmn.workflowengine.tenancy.domain.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByIdAndStatus(UUID id, TenantStatus status);
}
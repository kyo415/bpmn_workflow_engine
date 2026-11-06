package com.bpmn.workflowengine.workflow.domain;

import com.bpmn.workflowengine.tenancy.domain.Tenant;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "workflows",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_workflows_tenant_code",
                        columnNames = {"tenant_id", "code"}
                )
        }
)
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkflowStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Workflow() {
    }

    public Workflow(Tenant tenant, String code, String name, String description) {
        this.tenant = tenant;
        this.code = code;
        this.name = name;
        this.description = description;
        this.status = WorkflowStatus.DRAFT;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void activate() {
        this.status = WorkflowStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = WorkflowStatus.INACTIVE;
    }
}
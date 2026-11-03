package com.bpmn.workflowengine.identity.domain;

import com.bpmn.workflowengine.tenancy.domain.Tenant;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "user_tenants",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_tenants_user_tenant",
            columnNames = {"user_id", "tenant_id"}
        )
    }
)
public class UserTenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    protected UserTenant() {
    }

    public UserTenant(User user, Tenant tenant, Role role) {
        this.user = user;
        this.tenant = tenant;
        this.role = role;
        this.status = MembershipStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public Role getRole() {
        return role;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void suspend() {
        this.status = MembershipStatus.SUSPENDED;
    }

    public void activate() {
        this.status = MembershipStatus.ACTIVE;
    }

    public void changeRole(Role role) {
        this.role = role;
    }
}

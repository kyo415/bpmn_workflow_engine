
package com.bpmn.workflowengine.identity.domain;

import com.bpmn.workflowengine.tenancy.domain.Tenant;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "invitation_tokens",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_invitation_tokens_hash",
            columnNames = "token_hash"

        )

    }
)
public class InvitationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "invited_email", nullable = false, length = 255)
    private String invitedEmail;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected InvitationToken() {
    }

    public InvitationToken(
        Tenant tenant,
        Role role,
        User createdBy,
        String invitedEmail,
        String tokenHash,
        Instant expiresAt
        ) {
            this.tenant = tenant;
            this.role = role;
            this.createdBy = createdBy;
            this.invitedEmail = invitedEmail;
            this.tokenHash = tokenHash;
            this.expiresAt = expiresAt;
        }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public Role getRole() {
        return role;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isUsable(Instant now) {
        return usedAt == null
                && revokedAt == null
                && expiresAt.isAfter(now);
    }

    public void markAsUsed(Instant usedAt) {
        if (!isUsable(usedAt)) {
            throw new IllegalStateException(
                    "Invitation token is no longer usable"
            );
        }

        this.usedAt = usedAt;
    }

    public void revoke(Instant revokedAt) {
        if (usedAt != null || this.revokedAt != null) {
            throw new IllegalStateException(
                    "Invitation token cannot be revoked"
            );
        }

        this.revokedAt = revokedAt;
    }

    public String getInvitedEmail() {
        return invitedEmail;
    }
}

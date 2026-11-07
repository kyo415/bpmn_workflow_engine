package com.bpmn.workflowengine.identity.service;

import com.bpmn.workflowengine.identity.domain.*;
import com.bpmn.workflowengine.identity.repository.*;
import com.bpmn.workflowengine.tenancy.domain.Tenant;
import com.bpmn.workflowengine.tenancy.domain.TenantStatus;
import com.bpmn.workflowengine.tenancy.repository.TenantRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class InvitationTokenService {

    private static final Duration INVITATION_VALIDITY =
            Duration.ofDays(7);

    private static final Set<String> ALLOWED_ROLES = Set.of(
            "TENANT_ADMIN",
            "LOGISTICS_MANAGER",
            "OPERATOR"
    );

    private final InvitationTokenRepository invitationTokenRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserTenantRepository userTenantRepository;
    private final RoleRepository roleRepository;
    private final InvitationTokenGenerator tokenGenerator;
    private final Clock clock;

    public InvitationTokenService(
            InvitationTokenRepository invitationTokenRepository,
            UserRepository userRepository,
            TenantRepository tenantRepository,
            UserTenantRepository userTenantRepository,
            RoleRepository roleRepository,
            InvitationTokenGenerator tokenGenerator,
            Clock clock
    ) {
        this.invitationTokenRepository = invitationTokenRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.userTenantRepository = userTenantRepository;
        this.roleRepository = roleRepository;
        this.tokenGenerator = tokenGenerator;
        this.clock = clock;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Invited email is required"
            );
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (normalizedEmail.length() > 255
                || !normalizedEmail.matches(
                        "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$"
                )) {
            throw new IllegalArgumentException(
                    "Invalid email address"
            );
        }

        return normalizedEmail;
    }

    private void validateInvitationCreator(
            UUID userId,
            UUID tenantId
    ) {
        boolean activeUser = userRepository.existsByIdAndStatus(
                userId,
                UserStatus.ACTIVE
        );

        if (!activeUser) {
            throw new IllegalArgumentException(
                    "User is not active or does not exist"
            );
        }

        boolean activeTenant = tenantRepository.existsByIdAndStatus(
                tenantId,
                TenantStatus.ACTIVE
        );

        if (!activeTenant) {
            throw new IllegalArgumentException(
                    "Tenant is not active or does not exist"
            );
        }

        boolean isTenantAdmin =
                userTenantRepository.existsByUser_IdAndTenant_IdAndStatusAndRole_Code(
                        userId,
                        tenantId,
                        MembershipStatus.ACTIVE,
                        "TENANT_ADMIN"
                );

        if (!isTenantAdmin) {
            throw new IllegalArgumentException(
                    "Only active Tenant Admin can create invitations"
            );
        }
    }

    @Transactional
    public String createInvitation(
            UUID creatorUserId,
            UUID tenantId,
            String invitedEmail,
            String roleCode
    ) {
        validateInvitationCreator(creatorUserId, tenantId);

        String normalizedEmail = normalizeEmail(invitedEmail);

        if (roleCode == null || !ALLOWED_ROLES.contains(roleCode)) {
            throw new IllegalArgumentException(
                    "Invalid invitation role"
            );
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tenant not found"
                ));

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Role not found"
                ));

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Creator not found"
                ));

        String rawToken = tokenGenerator.generateToken();
        String tokenHash = tokenGenerator.hashToken(rawToken);

        Instant now = clock.instant();
        Instant expiresAt = now.plus(INVITATION_VALIDITY);

        InvitationToken invitation = new InvitationToken(
                tenant,
                role,
                creator,
                normalizedEmail,
                tokenHash,
                expiresAt
        );

        invitationTokenRepository.save(invitation);

        // Raw token is returned once; only its hash is stored.
        return rawToken;
    }

    @Transactional
    public void acceptInvitation(
            UUID userId,
            String rawToken
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Invitation token is required"
            );
        }

        String tokenHash = tokenGenerator.hashToken(rawToken);
        Instant now = clock.instant();

        InvitationToken invitation = invitationTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invitation token is invalid"
                ));

        if (!invitation.isUsable(now)) {
            throw new IllegalArgumentException(
                    "Invitation token is expired, used or revoked"
            );
        }

        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User is not active or does not exist"
                ));

        String userEmail = normalizeEmail(user.getEmail());

        if (!userEmail.equals(invitation.getInvitedEmail())) {
            throw new IllegalArgumentException(
                    "Invitation email does not match account email"
            );
        }

        UUID tenantId = invitation.getTenant().getId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tenant is not active or does not exist"
                ));

        boolean alreadyMember =
                userTenantRepository.existsByUser_IdAndTenant_Id(
                        userId,
                        tenantId
                );

        if (alreadyMember) {
            throw new IllegalArgumentException(
                    "User is already a member of this tenant"
            );
        }

        UserTenant membership = new UserTenant(
                user,
                tenant,
                invitation.getRole()
        );

        userTenantRepository.save(membership);

        invitation.markAsUsed(now);

        invitationTokenRepository.save(invitation);
    }

    @Transactional
    public void revokeInvitation(
            UUID creatorUserId,
            UUID tenantId,
            UUID invitationId
    ) {
        validateInvitationCreator(creatorUserId, tenantId);

        InvitationToken invitation = invitationTokenRepository
                .findByIdForUpdate(invitationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invitation not found"
                ));

        if (!invitation.getTenant().getId().equals(tenantId)) {
            throw new IllegalArgumentException(
                    "Invitation does not belong to this tenant"
            );
        }

        if (invitation.getUsedAt() != null) {
            throw new IllegalArgumentException(
                    "Used invitation cannot be revoked"
            );
        }

        if (invitation.getRevokedAt() != null) {
            throw new IllegalArgumentException(
                    "Invitation has already been revoked"
            );
        }

        invitation.revoke(clock.instant());

        invitationTokenRepository.save(invitation);
    }
}
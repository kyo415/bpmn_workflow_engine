package com.bpmn.workflowengine.identity.service;

import com.bpmn.workflowengine.identity.domain.*;
import com.bpmn.workflowengine.identity.repository.InvitationTokenRepository;
import com.bpmn.workflowengine.identity.repository.RoleRepository;
import com.bpmn.workflowengine.identity.repository.UserRepository;
import com.bpmn.workflowengine.identity.repository.UserTenantRepository;
import com.bpmn.workflowengine.tenancy.domain.Tenant;
import com.bpmn.workflowengine.tenancy.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class InvitationTokenServiceIntegrationTest {

    @Autowired
    private InvitationTokenService invitationTokenService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTenantRepository userTenantRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private InvitationTokenRepository invitationTokenRepository;

    @Autowired
    private InvitationTokenGenerator tokenGenerator;

    private Tenant tenant;
    private User admin;
    private User invitee;
    private User otherUser;

    private Role tenantAdminRole;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString();

        // Load default roles seeded by Flyway
        tenantAdminRole = roleRepository.findByCode("TENANT_ADMIN")
                .orElseThrow();

        // Create tenant
        tenant = tenantRepository.saveAndFlush(
                new Tenant("Test Logistics", "TEST_" + suffix)
        );

        // Create Tenant Admin
        admin = userRepository.saveAndFlush(
                new User(
                        "admin_" + suffix + "@example.com",
                        "hashed_password",
                        "Test Admin"
                )
        );

        // Create invited user
        invitee = userRepository.saveAndFlush(
                new User(
                        "invitee_" + suffix + "@example.com",
                        "hashed_password",
                        "Test Invitee"
                )
        );

        // Create another user
        otherUser = userRepository.saveAndFlush(
                new User(
                        "other_" + suffix + "@example.com",
                        "hashed_password",
                        "Other User"
                )
        );

        // Assign Tenant Admin membership
        userTenantRepository.saveAndFlush(
                new UserTenant(admin, tenant, tenantAdminRole)
        );
    }

    private String createInvitation() {
        return invitationTokenService.createInvitation(
                admin.getId(),
                tenant.getId(),
                invitee.getEmail(),
                "OPERATOR"
        );
    }

    @Test
        void shouldCreateInvitationSuccessfully() {
            String rawToken = createInvitation();

            assertNotNull(rawToken);
            assertFalse(rawToken.isBlank());

            String tokenHash = tokenGenerator.hashToken(rawToken);

            InvitationToken invitation = invitationTokenRepository
                    .findByTokenHash(tokenHash)
                    .orElseThrow();

            assertEquals(invitee.getEmail(), invitation.getInvitedEmail());
            assertNotNull(invitation.getExpiresAt());
            assertTrue(invitation.isUsable(Instant.now()));
        }

    @Test
    void shouldAcceptInvitationWithMatchingEmail() {
        String rawToken = createInvitation();

        invitationTokenService.acceptInvitation(
                invitee.getId(),
                rawToken
        );

        assertTrue(
                userTenantRepository.existsByUser_IdAndTenant_IdAndStatusAndRole_Code(
                        invitee.getId(),
                        tenant.getId(),
                        MembershipStatus.ACTIVE,
                        "OPERATOR"
                )
        );

         InvitationToken invitation = invitationTokenRepository
                        .findByTokenHash(tokenGenerator.hashToken(rawToken))
                        .orElseThrow();

        assertNotNull(invitation.getUsedAt());
        assertFalse(invitation.isUsable(Instant.now()));
    }

    @Test
    void shouldRejectInvitationWithDifferentEmail() {
        String rawToken = createInvitation();

        assertThrows(
                IllegalArgumentException.class,
                () -> invitationTokenService.acceptInvitation(
                        otherUser.getId(),
                        rawToken
                )
        );

        assertFalse(
                userTenantRepository.existsByUser_IdAndTenant_Id(
                        otherUser.getId(),
                        tenant.getId()
                )
        );
    }

    @Test
    void shouldRejectInvitationCreatedByNonAdmin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invitationTokenService.createInvitation(
                        otherUser.getId(),
                        tenant.getId(),
                        invitee.getEmail(),
                        "OPERATOR"
                )
        );
    }

    @Test
    void shouldRejectRevokedInvitation() {
        String rawToken = createInvitation();

        UUID invitationId = invitationTokenRepository
                .findByTokenHash(tokenGenerator.hashToken(rawToken))
                .orElseThrow()
                .getId();

        invitationTokenService.revokeInvitation(
                admin.getId(),
                tenant.getId(),
                invitationId
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> invitationTokenService.acceptInvitation(
                        invitee.getId(),
                        rawToken
                )
        );
    }

    @Test
    void shouldRejectAlreadyUsedInvitation() {
        String rawToken = createInvitation();

        invitationTokenService.acceptInvitation(
                invitee.getId(),
                rawToken
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> invitationTokenService.acceptInvitation(
                        invitee.getId(),
                        rawToken
                )
        );
    }
}

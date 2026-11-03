package com.bpmn.workflowengine.identity.domain;

import com.bpmn.workflowengine.tenancy.domain.Tenant;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class InvitationTokenTest {

    private InvitationToken createToken() {
        Tenant tenant = new Tenant("ABC Logistics", "ABC");

        Role role = new Role(
                "OPERATOR",
                "Operator",
                "Handles logistics tasks"
        );

        User creator = new User(
                "admin@example.com",
                "hashed-password",
                "Admin"
        );

        Instant now = Instant.parse("2026-11-03T10:00:00Z");

        return new InvitationToken(
                tenant,
                role,
                creator,
                "member@example.com",
                "hashed-token",
                now.plus(Duration.ofDays(7))
        );
    }

    @Test
    void shouldBeUsableWhenNotExpiredOrUsedOrRevoked() {
        InvitationToken token = createToken();

        Instant now = Instant.parse("2026-11-03T10:00:00Z");

        assertTrue(token.isUsable(now));
    }

    @Test
    void shouldNotBeUsableWhenExpired() {
        InvitationToken token = createToken();

        Instant expiredTime = Instant.parse("2026-11-11T10:00:00Z");

        assertFalse(token.isUsable(expiredTime));
    }

    @Test
    void shouldNotBeUsableAfterBeingUsed() {
        InvitationToken token = createToken();

        Instant now = Instant.parse("2026-11-03T10:00:00Z");

        token.markAsUsed(now);

        assertNotNull(token.getUsedAt());
        assertFalse(token.isUsable(now));
    }

    @Test
    void shouldNotBeUsableAfterBeingRevoked() {
        InvitationToken token = createToken();

        Instant now = Instant.parse("2026-11-03T10:00:00Z");

        token.revoke(now);

        assertNotNull(token.getRevokedAt());
        assertFalse(token.isUsable(now));
    }

    @Test
    void shouldNotAllowUsingTokenTwice() {
        InvitationToken token = createToken();

        Instant now = Instant.parse("2026-11-03T10:00:00Z");

        token.markAsUsed(now);

        assertThrows(
                IllegalStateException.class,
                () -> token.markAsUsed(now.plusSeconds(1))
        );
    }

    @Test
    void shouldNotAllowRevokingUsedToken() {
        InvitationToken token = createToken();

        Instant now = Instant.parse("2026-11-03T10:00:00Z");

        token.markAsUsed(now);

        assertThrows(
                IllegalStateException.class,
                () -> token.revoke(now.plusSeconds(1))
        );
    }

    @Test
    void shouldNotAllowRevokingTokenTwice() {
        InvitationToken token = createToken();

        Instant now = Instant.parse("2026-11-03T10:00:00Z");

        token.revoke(now);

        assertThrows(
                IllegalStateException.class,
                () -> token.revoke(now.plusSeconds(1))
        );
    }

    @Test
    void shouldStoreInvitedEmail() {
        InvitationToken token = createToken();

        assertEquals("member@example.com", token.getInvitedEmail());
    }
}

package com.bpmn.workflowengine.identity.infrastructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvitationTokenGeneratorTest {

    private final InvitationTokenGenerator generator =
            new InvitationTokenGenerator();

    @Test
    void shouldGenerateNonBlankToken() {
        String token = generator.generateToken();

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldGenerateUniqueTokens() {
        String token1 = generator.generateToken();
        String token2 = generator.generateToken();

        assertNotEquals(token1, token2);
    }

    @Test
    void shouldGenerateTokenWithExpectedLength() {
        String token = generator.generateToken();

        // 32 random bytes encoded as Base64 URL-safe without padding
        assertEquals(43, token.length());
    }

    @Test
    void shouldGenerateSameHashForSameToken() {
        String token = "sample-invitation-token";

        String hash1 = generator.hashToken(token);
        String hash2 = generator.hashToken(token);

        assertEquals(hash1, hash2);
    }

    @Test
    void shouldGenerateDifferentHashesForDifferentTokens() {
        String hash1 = generator.hashToken("token-one");
        String hash2 = generator.hashToken("token-two");

        assertNotEquals(hash1, hash2);
    }

    @Test
    void shouldGenerateSha256HashWith64HexCharacters() {
        String hash = generator.hashToken("sample-token");

        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void shouldRejectNullToken() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.hashToken(null)
        );
    }

    @Test
    void shouldRejectBlankToken() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.hashToken(" ")
        );
    }
}

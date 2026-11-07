
package com.bpmn.workflowengine.identity.repository;

import com.bpmn.workflowengine.identity.domain.InvitationToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InvitationTokenRepository
        extends JpaRepository<InvitationToken, UUID> {

    Optional<InvitationToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT invitation
            FROM InvitationToken invitation
            WHERE invitation.tokenHash = :tokenHash
            """)
    Optional<InvitationToken> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT invitation
            FROM InvitationToken invitation
            WHERE invitation.id = :invitationId
            """)
    Optional<InvitationToken> findByIdForUpdate(
            @Param("invitationId") UUID invitationId
    );
}

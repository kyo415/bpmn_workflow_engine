package com.bpmn.workflowengine.identity.repository;

import com.bpmn.workflowengine.identity.domain.User;
import com.bpmn.workflowengine.identity.domain.UserStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByIdAndStatus(UUID id, UserStatus status);
}

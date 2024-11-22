package com.epartner.userManagementService.repository;

import com.epartner.userManagementService.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Find a user by username
    Optional<UserEntity> findByEmail(String email);

    // Check if a user exists by email
    boolean existsByEmail(String email);

    // Check if a user exists by phone
    boolean existsByPhone(String phone);
}


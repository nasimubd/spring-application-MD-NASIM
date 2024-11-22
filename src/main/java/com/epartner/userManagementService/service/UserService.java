package com.epartner.userManagementService.service;

import com.epartner.userManagementService.dto.UserDTO;
import com.epartner.userManagementService.exception.ResourceNotFoundException;
import com.epartner.userManagementService.model.UserEntity;
import com.epartner.userManagementService.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new user with password hashing
     */
    public UserEntity registerUser(UserEntity user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists: " + user.getPhone());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash password
        return userRepository.save(user);
    }


    /**
     * Get all users with pagination and convert to DTOs
     */
    public List<UserDTO> findAllUsers(int page, int size) {
        Page<UserEntity> usersPage = userRepository.findAll(PageRequest.of(page, size));
        return usersPage.getContent().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Find a user by ID
     */
    public Optional<UserEntity> findUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Delete a user by ID
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Update user details
     */
    public UserEntity updateUser(Long id, UserEntity userDetails) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check for unique phone constraint
        if (userRepository.existsByPhone(userDetails.getPhone()) && !user.getPhone().equals(userDetails.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists: " + userDetails.getPhone());
        }

        // Update user details
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setBusinessName(userDetails.getBusinessName());
        user.setUsername(userDetails.getUsername());

        return userRepository.save(user);
    }


    /**
     * Update user role
     */
    public UserEntity updateUserRole(Long id, UserEntity.Role newRole) {
        log.info("Updating user ID {} with role: {}", id, newRole);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setRole(newRole); // Assign role directly
        return userRepository.save(user);
    }




    public void updatePassword(Long id, String newPassword) {
        // Validate new password
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }

        // Retrieve the user by ID
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check if the new password is the same as the old one
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as the current password.");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword)); // Hash new password
        userRepository.save(user);

        log.info("Password updated for user with ID: {}", id); // Log the update event
    }

    /**
     * Convert UserEntity to UserDTO
     */
    public UserDTO toDTO(UserEntity user) {
        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getBusinessName(),
                user.getRole().name()
        );
    }
}

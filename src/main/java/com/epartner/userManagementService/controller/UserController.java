package com.epartner.userManagementService.controller;

import com.epartner.userManagementService.dto.UserDTO;
import com.epartner.userManagementService.model.UserEntity;
import com.epartner.userManagementService.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserEntity user) {
        UserEntity savedUser = userService.registerUser(user);
        UserDTO userDTO = userService.toDTO(savedUser);
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }

    /**
     * Get all users with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<UserDTO> users = userService.findAllUsers(page, size);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Get a user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userSecurity.hasUserId(authentication, #id)")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.findUserById(id)
                .map(user -> new ResponseEntity<>(userService.toDTO(user), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Delete a user by ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update user details
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userSecurity.hasUserId(authentication, #id)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserEntity userDetails) {
        try {
            UserDTO updatedUser = userService.toDTO(userService.updateUser(id, userDetails));
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "An unexpected error occurred",
                    "details", e.getMessage()
            ));
        }
    }


    /**
     * Update user role
     */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable Long id, @RequestBody String newRole) {
        try {
            // Convert the role string to the enum
            UserEntity.Role role = UserEntity.Role.valueOf(newRole.toUpperCase());

            // Call the service to update the role
            UserEntity updatedUser = userService.updateUserRole(id, role);

            // Return the updated user as a DTO
            return new ResponseEntity<>(userService.toDTO(updatedUser), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Invalid role provided: {}", newRole);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 for invalid role
        }
    }



    /**
     * Update user password
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userSecurity.hasUserId(authentication, #id)")
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @RequestBody String newPassword) {
        userService.updatePassword(id, newPassword);
        return ResponseEntity.ok().build();
    }

//    test one
    @GetMapping("/admin-or-manager")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
        public ResponseEntity<String> adminOrManagerEndpoint() {
            return ResponseEntity.ok("Welcome, Admin or Manager!");
        }
}

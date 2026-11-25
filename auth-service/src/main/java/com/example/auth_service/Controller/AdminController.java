package com.example.auth_service.Controller;

import com.example.auth_service.Dto.UpdateRoleRequest;
import com.example.auth_service.Entity.User;
import com.example.auth_service.Service.RoleManagementService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RoleManagementService roleManagementService;

    public AdminController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        User updatedUser = roleManagementService.updateUserRole(
                userId, request, jwt.getSubject()
        );

        return ResponseEntity.ok(Map.of(
                "message", "User role updated successfully",
                "userId", updatedUser.getId(),
                "email", updatedUser.getEmail(),
                "role", updatedUser.getRole()
        ));
    }
}
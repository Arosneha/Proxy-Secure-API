package com.example.auth_service.Dto;

import jakarta.validation.constraints.NotNull;
import com.example.auth_service.Enums.Role;

public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
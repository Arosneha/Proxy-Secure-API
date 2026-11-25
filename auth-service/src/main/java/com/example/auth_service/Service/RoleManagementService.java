
package com.example.auth_service.Service;

import com.example.auth_service.Dto.UpdateRoleRequest;
import com.example.auth_service.Entity.User;
import com.example.auth_service.Enums.Role;
import com.example.auth_service.Repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleManagementService {

    private final UserRepository userRepository;

    public RoleManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User updateUserRole(
            Long userId,
            UpdateRoleRequest request,
            String adminEmail) {

        Role newRole = request.getRole();

        if (newRole == null) {
            throw new IllegalArgumentException("Role is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new IllegalArgumentException(
                    "ADMIN cannot change their own role"
            );
        }

        user.setRole(newRole);

        return userRepository.save(user);
    }
}

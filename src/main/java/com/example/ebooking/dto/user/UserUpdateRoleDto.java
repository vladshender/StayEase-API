package com.example.ebooking.dto.user;

import com.example.ebooking.model.Role;
import jakarta.validation.constraints.NotNull;

public record UserUpdateRoleDto(@NotNull Role.RoleName role) {
}

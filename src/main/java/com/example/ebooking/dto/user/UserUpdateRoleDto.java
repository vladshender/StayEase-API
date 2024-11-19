package com.example.ebooking.dto.user;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record UserUpdateRoleDto(@NotNull Set<String> roles) {
}

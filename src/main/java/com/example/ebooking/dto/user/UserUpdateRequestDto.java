package com.example.ebooking.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequestDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}

package com.example.ebooking.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginRequestDto {
    @NotBlank
    @Size(min = 8, max = 20)
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;
}

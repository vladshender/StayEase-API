package com.example.ebooking.dto.user;

import com.example.ebooking.validation.FieldMatch;
import lombok.Data;

@FieldMatch(first = "password", second = "repeatPassword")
@Data
public class UserUpdatePasswordDto {
    private String password;
    private String repeatPassword;
}

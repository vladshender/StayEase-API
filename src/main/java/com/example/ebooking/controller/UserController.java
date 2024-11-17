package com.example.ebooking.controller;

import com.example.ebooking.dto.user.UserResponseDto;
import com.example.ebooking.dto.user.UserUpdatePasswordDto;
import com.example.ebooking.dto.user.UserUpdateRequestDto;
import com.example.ebooking.dto.user.UserUpdateRoleDto;
import com.example.ebooking.model.User;
import com.example.ebooking.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    private UserResponseDto updateUserRoleByUserId(@PathVariable Long id,
                                                   @RequestBody UserUpdateRoleDto updateRoleDto) {
        return userService.updateRole(id, updateRoleDto);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/me")
    private UserResponseDto getUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userService.getInfoByUser(user);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PutMapping("/me")
    private UserResponseDto updateUser(Authentication authentication,
                                           @RequestBody UserUpdateRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return userService.updateUser(user, requestDto);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PutMapping("/me/password")
    private String updateUserPassword(Authentication authentication,
                                      @Valid @RequestBody UserUpdatePasswordDto updatePasswordDto) {
        User user = (User) authentication.getPrincipal();
        return userService.updatePasswordByUser(user, updatePasswordDto);
    }
}

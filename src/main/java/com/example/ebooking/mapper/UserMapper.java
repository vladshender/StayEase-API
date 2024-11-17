package com.example.ebooking.mapper;

import com.example.ebooking.config.MapperConfig;
import com.example.ebooking.dto.user.UserRegistrationRequestDto;
import com.example.ebooking.dto.user.UserResponseDto;
import com.example.ebooking.dto.user.UserUpdateRequestDto;
import com.example.ebooking.model.Role;
import com.example.ebooking.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toDto(User user);

    User toModel(UserRegistrationRequestDto requestDto);

    void updateUserFromDB(UserUpdateRequestDto requestDto, @MappingTarget User user);

    default Set<String> mapRoles(Set<Role> roles) {
        return roles.stream()
                .map(role -> role.getRole().name().toString())
                .collect(Collectors.toSet());
    }

    @AfterMapping
    default void setRoles(@MappingTarget UserResponseDto responseDto, User user) {
        responseDto.setRoles(mapRoles(user.getRoles()));
    }
}

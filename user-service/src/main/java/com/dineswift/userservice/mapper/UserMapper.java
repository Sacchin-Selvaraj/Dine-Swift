package com.dineswift.userservice.mapper;

import com.dineswift.userservice.model.entites.Role;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.model.request.UserRequest;
import com.dineswift.userservice.model.response.RoleDto;
import com.dineswift.userservice.model.response.UserDTO;
import com.dineswift.userservice.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ModelMapper modelMapper;


    public User toEntity(UserRequest userRequest) {
        return modelMapper.map(userRequest, User.class);
    }

    public UserDTO toDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public UserResponse toUserResponse(User user) {
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        if (!user.getRoles().isEmpty()){
            userResponse.setRoles(user.getRoles().stream().map(this::toRoleDto).collect(Collectors.toSet()));
        }
        log.info("Mapped user to Response DTO");
        return userResponse;
    }

    public RoleDto toRoleDto(Role role) {
        RoleDto roleDto = new RoleDto();
        roleDto.setRoleName(role.getRoleName());
        roleDto.setRoleNameString(role.getRoleName().getDisplayName());
        log.info("Mapped Role to RoleDto: {}", roleDto);
        return roleDto;
    }
}

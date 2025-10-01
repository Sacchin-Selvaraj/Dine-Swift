package com.dineswift.userservice.mapper;

import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.model.request.UserRequest;
import com.dineswift.userservice.model.response.UserDTO;
import com.dineswift.userservice.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

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
        return modelMapper.map(user, UserResponse.class);
    }
}

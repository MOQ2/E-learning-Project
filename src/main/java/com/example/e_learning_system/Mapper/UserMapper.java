package com.example.e_learning_system.Mapper;

import com.example.e_learning_system.Dto.UserResponseDTO;
import com.example.e_learning_system.Entities.UserEntity;


public class UserMapper {

    public static UserResponseDTO toUserResponseDTO (UserEntity userEntity) {
        return new UserResponseDTO(userEntity);
    }







}

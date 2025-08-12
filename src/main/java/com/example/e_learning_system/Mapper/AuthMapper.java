package com.example.e_learning_system.Mapper;
import com.example.e_learning_system.Dto.RegisterRequestDTO;
import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Entities.UserEntity;
import org.springframework.stereotype.Component;

@Component

public class AuthMapper {
    public UserEntity dtoToEntity(RegisterRequestDTO registerRequestDTO, RolesEntity rolesEntity){
        UserEntity userEntity = new UserEntity();
        userEntity.setName(registerRequestDTO.getName());
        userEntity.setPassword(registerRequestDTO.getPassword());
        userEntity.setPhone(registerRequestDTO.getPhone());
        userEntity.setEmail(registerRequestDTO.getEmail());
        userEntity.setRole(rolesEntity);
        userEntity.setIsActive(true);
        return userEntity;
    }
}

package com.example.e_learning_system.Mapper;
import com.example.e_learning_system.Dto.RegisterRequest;
import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    public UserEntity DtoToEntity(RegisterRequest registerRequest , RolesEntity rolesEntity){
        UserEntity userEntity = new UserEntity();
        userEntity.setName(registerRequest.getName());
        userEntity.setPassword(registerRequest.getPassword());
        userEntity.setPhone(registerRequest.getPhone());
        userEntity.setEmail(registerRequest.getEmail());
        userEntity.setRole(rolesEntity);
        userEntity.setIsActive(true);
        return userEntity;
    }
}

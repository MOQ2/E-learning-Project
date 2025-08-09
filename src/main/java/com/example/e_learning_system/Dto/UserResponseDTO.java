package com.example.e_learning_system.Dto;


import com.example.e_learning_system.Entities.PermissionsEntity;
import com.example.e_learning_system.Entities.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private int id;
    private String name ;
    private String role;
    private String email;
    private String phone;
    RoleResponseDTO roleResponse;

    public UserResponseDTO (UserEntity user) {
        this.name = user.getName();
        this.role = user.getRole().getName().name();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.id = user.getId();
        this.roleResponse = new RoleResponseDTO(user.getRole());

    }

}

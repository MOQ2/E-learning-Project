package com.example.e_learning_system.Dto;

import com.example.e_learning_system.Entities.PermissionsEntity;
import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Entities.RolesPermissionsEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDTO {
    private int id ;
    private String role;
    private String description;
    private Set<PermissionsResponsDTO> rolePermissions;
    public RoleResponseDTO (RolesEntity role) {
        if(role != null) {
            this.id = role.getId();
            this.role=role.getName().name();
            this.description = role.getDescription();

            this.rolePermissions = setPermissions(role.getRolePermissions());
        }

    }



    private Set<PermissionsResponsDTO> setPermissions(Set<RolesPermissionsEntity> rolePermissions) {
        return rolePermissions == null ? new HashSet<>() :
                rolePermissions.stream()
                        .map(RolesPermissionsEntity::getPermission)
                        .map(PermissionsResponsDTO::new)
                        .collect(Collectors.toSet());
    }


}

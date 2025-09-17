package com.example.e_learning_system.Dto;

import com.example.e_learning_system.Entities.PermissionsEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionsResponsDTO {
    private int id;
    private String name ;
    private String description;

    public PermissionsResponsDTO(PermissionsEntity permissionEntity) {
        if (permissionEntity != null) {
            this.id = permissionEntity.getId();
            this.name = permissionEntity.getName();
            this.description = permissionEntity.getDescription();
        }else  {
            this.id = -1;
            this.name = "";
            this.description = "";
        }

    }


}

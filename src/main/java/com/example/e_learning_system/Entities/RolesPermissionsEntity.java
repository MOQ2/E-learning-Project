package com.example.e_learning_system.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolesPermissionsEntity extends BaseEntity {


    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    @JsonIgnoreProperties("rolePermissions")
    private RolesEntity role;

    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    @JsonIgnoreProperties("rolePermissions")
    private PermissionsEntity permission;

}

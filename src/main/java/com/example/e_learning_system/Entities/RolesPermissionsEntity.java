package com.example.e_learning_system.Entities;

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
    private RolesEntity role;

    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionsEntity permission;

}

package com.example.e_learning_system.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "role_permissions")
@Getter
@Setter
public class RolesPermissionsEntity extends BaseEntity {


    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private RolesEntity role;

    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionsEntity permission;

}

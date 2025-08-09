package com.example.e_learning_system.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions")
public class PermissionsEntity extends BaseEntity {
    @NotBlank
    @Column(name = "name")
    private String name;

    private String description;
    @OneToMany(mappedBy = "permission", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<RolesPermissionsEntity> rolePermissions;
}

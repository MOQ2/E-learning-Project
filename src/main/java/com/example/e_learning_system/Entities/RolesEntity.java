package com.example.e_learning_system.Entities;

import com.example.e_learning_system.Config.RolesName;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RolesEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private RolesName name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<RolesPermissionsEntity> rolePermissions;


    private Set<PermissionsEntity> permissions;

    @Override
    @Transient
    public String getEntityType() {
        return "RolesEntity";
    }
}

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
    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
    private Set<RolesPermissionsEntity> rolePermissions;

}

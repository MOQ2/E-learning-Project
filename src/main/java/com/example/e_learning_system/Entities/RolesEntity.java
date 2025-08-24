package com.example.e_learning_system.Entities;

import com.example.e_learning_system.Config.RolesName;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolesEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private RolesName name;

    @Column(name = "description")
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<PermissionsEntity> permissions;

    @Override
    @Transient
    public String getEntityType() {
        return "RolesEntity";
    }
}

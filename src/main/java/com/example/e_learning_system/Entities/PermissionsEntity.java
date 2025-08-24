package com.example.e_learning_system.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionsEntity extends BaseEntity {

    @NotBlank
    @Column(name = "name")
    private String name;

    private String description;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<RolesEntity> roles;
    @Override
    @Transient
    public String getEntityType() {
        return "PermissionsEntity";
    }

}

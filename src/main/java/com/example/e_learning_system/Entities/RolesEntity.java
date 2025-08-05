package com.example.e_learning_system.Entities;

import com.example.e_learning_system.Config.RolesName;
import jakarta.persistence.*;
import lombok.*;

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

}

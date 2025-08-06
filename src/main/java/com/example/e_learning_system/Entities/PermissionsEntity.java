package com.example.e_learning_system.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
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
}

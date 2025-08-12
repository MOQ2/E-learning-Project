package com.example.e_learning_system.Entities;
import jakarta.persistence.*;
import lombok.*;
@Setter
@Getter
@ToString
@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @Column(name = "email", length = 250, nullable = false, unique = true)
    private String email;

    @Column(name = "phone", length = 250, nullable = false)
    private String phone;

    @Column(name = "password", length = 250, nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RolesEntity role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

}
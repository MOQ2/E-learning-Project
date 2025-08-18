package com.example.e_learning_system.Entities;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@ToString
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
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


    // relations section
    @OneToMany(mappedBy = "uploadedBy",fetch = FetchType.LAZY , orphanRemoval = true , cascade = CascadeType.ALL)
    Set<VideoEntity> videoEntities = new HashSet<>();

    @OneToMany(mappedBy = "createdBy",fetch = FetchType.LAZY , orphanRemoval = true , cascade = CascadeType.ALL)
    Set<Module> modules =  new HashSet<>();

    @OneToMany(mappedBy = "createdBy",fetch = FetchType.LAZY , orphanRemoval = true , cascade = CascadeType.ALL)
    Set<Course> courses = new HashSet<>();

    @OneToMany(mappedBy = "uploadedBy",fetch = FetchType.LAZY , orphanRemoval = true , cascade = CascadeType.ALL)
    Set<Attachment> attachments = new HashSet<>();
}
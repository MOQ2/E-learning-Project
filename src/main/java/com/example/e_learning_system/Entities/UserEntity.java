package com.example.e_learning_system.Entities;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import java.time.LocalDateTime;

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


    //new feilds in join
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_picture_url", columnDefinition = "TEXT")
    private String profilePictureUrl;

    // end of new feild in join

    // relations section
    @OneToMany(mappedBy = "uploadedBy",fetch = FetchType.LAZY , orphanRemoval = true , cascade = CascadeType.ALL)
    Set<VideoEntity> videoEntities = new HashSet<>();

    @OneToMany(mappedBy = "createdBy",fetch = FetchType.LAZY , orphanRemoval = true , cascade = CascadeType.ALL)
    Set<Module> modules =  new HashSet<>();

    @OneToMany(mappedBy = "createdBy",fetch = FetchType.LAZY , orphanRemoval = true , cascade = CascadeType.ALL)
    Set<Course> courses = new HashSet<>();

    @OneToMany(mappedBy = "uploadedBy",fetch = FetchType.LAZY , orphanRemoval = true , cascade = CascadeType.ALL)
    Set<Attachment> attachments = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_watched_videos",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "video_id")
    )
    private Set<VideoEntity> watchedVideos = new HashSet<>();


    @Override
    @Transient
    public String getEntityType() {
        return "UserEntity";
    }
}
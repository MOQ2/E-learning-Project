package com.example.e_learning_system.Entities;


import com.example.e_learning_system.Config.CourseStatus;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Module extends BaseEntity{


    //! add the relationships from the database
    //! done >>> add the course status enum same as in course table
    //! add the access_model enum same as in course table >> helps when some courses have preview modules



    @Column(name ="name")
    String name ;
    @Column(name = "description")
    String description ;
    @Column(name = "is_active")
    boolean isActive;
    @Column(name = "estimated_duration_minutes")
    int estimatedDuration;
    @Column(name="status")
    @Enumerated(EnumType.STRING)
    CourseStatus  courseStatus = CourseStatus.DRAFT;


    // relations

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;


    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL , orphanRemoval = true)
    private Set<CourseModules> courseModules;

    @OneToMany(
            mappedBy = "module",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("videoOrder ASC")
    private List<ModuleVideos> moduleVideos = new ArrayList<>();




    @Transient
    Set <Course> getModules (){
        if(this.courseModules == null)
            return new HashSet<>();
        return  this.courseModules.stream().map(CourseModules::getCourse).collect(Collectors.toSet());
    }
}

package com.example.e_learning_system.Entities;


import com.example.e_learning_system.Config.AccessModel;
import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.DifficultyLevel;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {


    //! state enum
    //! access mode
    //! divficualty level // add a migration fix to make it enum not just a string
    //! add the relations between it and other tables.


    @Column(name = "name" , nullable = false)
    String name;
    @Column(name = "description")
    String description;
    @Column(name = "one_time_price")
    BigDecimal oneTimePrice;
    @Column(name = "currency")
    String currency ;
    @Column(name = "thumbnail_url")
    String thumbnail;
    @Column(name = "preview_video_url")
    String previewVideoUrl;
    @Column(name = "estimated_duration_hours")
    int estimatedDrationInHours;
    @Column(name = "is_active")
    boolean isActive;
    @Column(name = "is_free" , insertable = false , updatable = false)
    boolean isFree;
    // enums
    @Column(name = "status" , columnDefinition = "course_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    CourseStatus status= CourseStatus.DRAFT;

    @Column(name = "access_model" ,columnDefinition = "access_model")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)

    AccessModel accessModel ;

    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    DifficultyLevel difficultyLevel;




    //relations
    // created by a user
    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL , orphanRemoval = true)
    private Set<CourseModules> courseModules;



    @Transient
    public Set <Module> getModules (){
        if(this.courseModules == null)
            return new HashSet<>();
        return  this.courseModules.stream().map(CourseModules::getModule).collect(Collectors.toSet());
    }
}

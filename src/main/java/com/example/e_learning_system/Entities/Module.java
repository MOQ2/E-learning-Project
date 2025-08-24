package com.example.e_learning_system.Entities;


import com.example.e_learning_system.Config.CourseStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;

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
    private Set<ModuleVideos> moduleVideos = new HashSet<>();




    @Transient
    public Set <VideoEntity> getVideos (){
        if(this.moduleVideos == null)
            return new HashSet<>();
        return  this.moduleVideos.stream().map(ModuleVideos::getVideo).collect(Collectors.toSet());
    }

    public void addVideoToModule(ModuleVideos moduleVideo) {
        this.moduleVideos.add(moduleVideo);
    }
    public void removeVideoFromModule(ModuleVideos moduleVideo) {
        if(moduleVideos.contains(moduleVideo))
            this.moduleVideos.remove(moduleVideo);
    }

    public boolean isUniqOrder(int order){
        for(ModuleVideos moduleVideo : this.moduleVideos){
            if(moduleVideo.getVideoOrder() == order)
                return false;
        }
        return true;
    }

}

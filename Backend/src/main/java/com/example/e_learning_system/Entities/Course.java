package com.example.e_learning_system.Entities;



import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.Currency;
import com.example.e_learning_system.Config.DifficultyLevel;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;

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
@SuperBuilder
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
    @Column(name = "subscription_price_monthly")
    BigDecimal subscriptionPriceMonthly;
    @Column(name = "subscription_price_3_months")
    BigDecimal subscriptionPrice3Months;
    @Column(name = "subscription_price_6_months")
    BigDecimal subscriptionPrice6Months;
    @Column(name = "allows_subscription")
    @Builder.Default
    Boolean allowsSubscription = false;
    @Column(name = "currency", columnDefinition = "currency_type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    Currency currency;
    @Column(name = "required_plan_level")
    @Builder.Default
    Integer requiredPlanLevel = 1;
    @Column(name = "category")
    String category;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thumbnail")
    private Attachment thumbnail;

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
    @Builder.Default
    CourseStatus status = CourseStatus.DRAFT;



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

    // New relationships for simplified subscription system
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private Set<PackageCourse> packageCourses;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private Set<SimplePayment> payments;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private Set<UserCourseAccess> userCourseAccesses;

    @ManyToMany(
            fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            }
    )
    @JoinTable(
            name = "course_tags",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<TagsEntity> tags = new HashSet<>();


    @Transient
    public Set <Module> getModules (){
        if(this.courseModules == null)
            return new HashSet<>();
        return  this.courseModules.stream().map(CourseModules::getModule).collect(Collectors.toSet());
    }


    public void addCourseModules(CourseModules courseModule){

        if(courseModule == null)
            return;

        if (this.courseModules == null)
            this.courseModules = new HashSet<>();
        if (!isUniqOrder(courseModule.getModuleOrder())) {
            throw new IllegalArgumentException("Module order must be unique");
        }
        this.courseModules.add(courseModule);
    }

    public void removeCourseModules(CourseModules courseModule){
        if(courseModule == null)
            return;
        if (this.courseModules == null)
            return;
        this.courseModules.remove(courseModule);
    }

    public boolean isUniqOrder (int order){
        for(CourseModules module : this.courseModules){
            if(module.getModuleOrder() == order)
                return false;
        }
        return true;
    }

    public void addTag (TagsEntity tag){
        if(tag == null)
            return;

        this.tags.add(tag);
        tag.getCourses().add(this);
    }
    public void removeTag (TagsEntity tag){
        if(tag == null)
            return;

        this.tags.remove(tag);
        tag.getCourses().remove(this);
    }
}
package com.example.e_learning_system.Entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_modules", uniqueConstraints = {
        @UniqueConstraint(
                name = "unique_course_module_order",
                columnNames = {"course_id", "module_order"}
        )
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseModules extends BaseEntity {



    @Column(name = "module_order", nullable = false)
    private Integer moduleOrder;


    @Column(name = "is_active")
    private boolean isActive = true;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // relations
    @ManyToOne(fetch = FetchType.LAZY, optional = false )
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;



}
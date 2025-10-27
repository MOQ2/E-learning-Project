package com.example.e_learning_system.Entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "package_courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PackageCourse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private Package packageEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}

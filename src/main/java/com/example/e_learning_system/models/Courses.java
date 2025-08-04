package com.example.e_learning_system.models;

import com.example.e_learning_system.models.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Courses {


    @Column(nullable=false)
    private String name ;
    private String description ;
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private CourseStatus status = CourseStatus.DRAFT;
    // ADD THE RELTINO BETWEEN THE COURSE AND THE CREATOR .
}

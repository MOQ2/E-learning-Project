package com.example.e_learning_system.Entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quizz extends BaseEntity {

    //! add the realtion between the cours , created by ....
    //! status is enum and the same as the course status enum

    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "description")
    String description;
    @Column(name = "max_attempts")
    int maxAttempts ;
    @Column(name = "passing_score")
    BigDecimal passing_score;
    @Column(name = "time_limit_minutes")
    int timeLimitInMinutes;
    @Column(name = "is_active")
    boolean isActive;




}

package com.example.e_learning_system.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "quizzes")
public class QuizEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private VideoEntity video;


    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizQuestionEntity> questions;

    @Override
    @Transient
    public String getEntityType() {
        return "QuizEntity";
    }

}

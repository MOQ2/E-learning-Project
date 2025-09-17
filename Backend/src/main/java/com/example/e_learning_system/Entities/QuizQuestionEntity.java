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
@Table(name = "quiz_question")
public class QuizQuestionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizEntity quiz;

    @Column(name = "text", length = 500, nullable = false)
    private String text;

    @Column(name = "question_mark", nullable = false)
    private Float questionMark;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizOptionEntity> options;

    @Override
    @Transient
    public String getEntityType() {
        return "QuizQuestionEntity";
    }
}


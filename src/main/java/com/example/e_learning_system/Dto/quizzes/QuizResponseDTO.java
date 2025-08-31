package com.example.e_learning_system.Dto.quizzes;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseDTO {
    private Integer id;
    private String title;
    private Integer totalScore;
    private Boolean isActive;
    private Integer courseId;
    private List<QuizQuestionResponseDTO> questions;
}

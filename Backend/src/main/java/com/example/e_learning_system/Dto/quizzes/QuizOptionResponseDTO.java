package com.example.e_learning_system.Dto.quizzes;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionResponseDTO {
    private Integer id;
    private String text;
    private Boolean isCorrect;
}
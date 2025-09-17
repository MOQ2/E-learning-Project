package com.example.e_learning_system.Dto.quizzes;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionResponseDTO {
    private Integer id;
    private String text;
    private Float questionMark;
    private List<QuizOptionResponseDTO> options;
}
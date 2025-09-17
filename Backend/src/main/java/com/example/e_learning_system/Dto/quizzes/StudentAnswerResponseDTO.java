package com.example.e_learning_system.Dto.quizzes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswerResponseDTO {
    private Integer questionId;
    private String questionText;
    private Integer selectedOptionId;
    private String selectedOptionText;
    private Boolean isCorrect;
}

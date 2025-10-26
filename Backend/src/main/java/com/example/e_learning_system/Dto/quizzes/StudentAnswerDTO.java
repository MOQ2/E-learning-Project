package com.example.e_learning_system.Dto.quizzes;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StudentAnswerDTO {

    @NotNull(message = "Question ID is required")
    private Integer questionId;

    private Integer selectedOptionId;
}

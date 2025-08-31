package com.example.e_learning_system.Dto.quizzes;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QuizOptionCreateDTO {

    @NotBlank(message = "Option text is required")
    @Size(max = 255, message = "Option text must not exceed 255 characters")
    private String text;

    @NotNull(message = "isCorrect flag is required")
    private Boolean isCorrect;
}

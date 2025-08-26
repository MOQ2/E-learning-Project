package com.example.e_learning_system.Dto.quizzes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QuizQuestionCreateDTO {

    @NotBlank(message = "Question text is required")
    @Size(max = 500, message = "Question text must not exceed 500 characters")
    private String text;

    @NotNull(message = "Question mark is required")
    @Positive(message = "Question mark must be greater than 0")
    private Float questionMark;

    @NotEmpty(message = "Question must have at least one option")
    @Valid
    private List<@Valid QuizOptionCreateDTO> options;
}

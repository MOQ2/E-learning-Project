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
public class CreateQuizDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @NotNull(message = "Total score is required")
    @Positive(message = "Total score must be greater than 0")
    private Integer totalScore;

    private Boolean isActive = true;

    @NotEmpty(message = "Quiz must have at least one question")
    @Valid
    private List<@Valid QuizQuestionCreateDTO> questions;
}

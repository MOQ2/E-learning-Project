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
public class UpdateQuizQuestionDTO {

    private Integer id;

    @NotBlank(message = "Question text is required")
    @Size(max = 500)
    private String text;

    @NotNull
    @Positive
    private Float questionMark;

    @Valid
    private List<@Valid UpdateQuizOptionDTO> options;
}

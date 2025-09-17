package com.example.e_learning_system.Dto.quizzes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateQuizOptionDTO {

    private Integer id;

    @NotBlank
    @Size(max = 255)
    private String text;

    @NotNull
    private Boolean isCorrect;
}


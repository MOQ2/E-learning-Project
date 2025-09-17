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
public class UpdateQuizDTO {

    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @Positive(message = "Total score must be greater than 0")
    private Integer totalScore;

    private Boolean isActive;

    @Valid
    List<UpdateQuizQuestionDTO> questions ;
}

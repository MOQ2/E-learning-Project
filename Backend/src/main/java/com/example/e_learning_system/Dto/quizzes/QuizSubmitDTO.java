package com.example.e_learning_system.Dto.quizzes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QuizSubmitDTO {

    @NotNull(message = "Quiz ID is required")
    private Integer quizId;

    @Valid
    private List<StudentAnswerDTO> answers;

}
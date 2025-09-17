
package com.example.e_learning_system.Dto.quizzes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionResponseDTO {
    private Integer submissionId;
    private String userName;
    private Float score;
    private LocalDateTime submittedAt;
}


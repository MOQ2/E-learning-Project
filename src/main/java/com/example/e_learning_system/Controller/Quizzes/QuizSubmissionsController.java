package com.example.e_learning_system.Controller.Quizzes;

import com.example.e_learning_system.Dto.quizzes.QuizSubmissionResponseDTO;
import com.example.e_learning_system.Dto.quizzes.QuizSubmitDTO;
import com.example.e_learning_system.Dto.quizzes.StudentAnswerResponseDTO;
import com.example.e_learning_system.Service.Interfaces.QuizzesInterfaces.QuizSubmissions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuizSubmissionsController {

    private final QuizSubmissions quizSubmissions;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("submitQuiz")
    public ResponseEntity<QuizSubmissionResponseDTO> submitQuiz(@Valid @RequestBody QuizSubmitDTO quizSubmitDTO) {
        QuizSubmissionResponseDTO response = quizSubmissions.submitQuiz(quizSubmitDTO);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{quizId}/attempts")
    public ResponseEntity<List<QuizSubmissionResponseDTO>> getQuizAttempts(
            @PathVariable Integer quizId,
            @RequestParam(required = false) Integer userId) {
        List<QuizSubmissionResponseDTO> attempts = quizSubmissions.getQuizAttempts(quizId, userId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/{submissionId}/answers")
    public ResponseEntity<List<StudentAnswerResponseDTO>> getSubmissionAnswers(
            @PathVariable Integer submissionId) {
        List<StudentAnswerResponseDTO> answers = quizSubmissions.getSubmissionAnswers(submissionId);
        return ResponseEntity.ok(answers);
    }


}

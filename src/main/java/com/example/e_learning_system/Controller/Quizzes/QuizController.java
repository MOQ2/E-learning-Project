package com.example.e_learning_system.Controller.Quizzes;
import com.example.e_learning_system.Dto.quizzes.CreateQuizDTO;
import com.example.e_learning_system.Dto.quizzes.QuizResponseDTO;
import com.example.e_learning_system.Dto.quizzes.UpdateQuizDTO;
import com.example.e_learning_system.Service.Interfaces.QuizzesInterfaces.QuizInterface;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quizzes")
public class QuizController {

    private final QuizInterface quizInterface;

    public QuizController(QuizInterface quizInterface) {
        this.quizInterface = quizInterface;
    }
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping("/{courseId}")
    public ResponseEntity<?> createQuiz(@PathVariable Integer courseId,
                                        @Valid @RequestBody CreateQuizDTO createQuizDTO) {
        quizInterface.createQuiz(courseId, createQuizDTO);
        return ResponseEntity.ok().body("Quiz created successfully");
    }
    @PatchMapping("/{quizId}")
    public ResponseEntity<?> updateQuiz(
            @PathVariable Integer quizId,
            @Valid @RequestBody UpdateQuizDTO updateQuizDTO
    ) {
        quizInterface.updateQuiz(quizId, updateQuizDTO);
        return ResponseEntity.ok().body("Quiz updated successfully");
    }
    @GetMapping("/getQuizzes")
    public ResponseEntity<List<QuizResponseDTO>> getQuizzes(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer quizId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean isActive
    ) {
        return ResponseEntity.ok(quizInterface.getQuizzes(courseId, quizId, title, isActive));
    }
}

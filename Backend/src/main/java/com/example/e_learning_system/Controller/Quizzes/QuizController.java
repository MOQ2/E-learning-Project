package com.example.e_learning_system.Controller.Quizzes;
import com.example.e_learning_system.Dto.quizzes.CreateQuizDTO;
import com.example.e_learning_system.Dto.quizzes.QuizResponseDTO;
import com.example.e_learning_system.Dto.quizzes.UpdateQuizDTO;
import com.example.e_learning_system.Service.Interfaces.QuizzesInterfaces.QuizInterface;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @PreAuthorize("hasAuthority('course:write')")
    @PostMapping("/{videoId}")
    public ResponseEntity<QuizResponseDTO> createQuiz(
            @PathVariable Integer videoId,
            @Valid @RequestBody CreateQuizDTO createQuizDTO) {

        QuizResponseDTO createdQuiz = quizInterface.createQuiz(videoId, createQuizDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }


    @PatchMapping("/{quizId}")
    public ResponseEntity<QuizResponseDTO> updateQuiz(
            @PathVariable Integer quizId,
            @Valid @RequestBody UpdateQuizDTO updateQuizDTO
    ) {
        QuizResponseDTO updatedQuiz = quizInterface.updateQuiz(quizId, updateQuizDTO);
        return ResponseEntity.ok(updatedQuiz);
    }



    @GetMapping("/getQuizzes")
    public ResponseEntity<List<QuizResponseDTO>> getQuizzes(
            @RequestParam(required = false) Integer videoId,
            @RequestParam(required = false) Integer quizId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean isActive
    ) {
        return ResponseEntity.ok(quizInterface.getQuizzes(videoId, quizId, title, isActive));
    }

}

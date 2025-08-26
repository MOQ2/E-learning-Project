package com.example.e_learning_system.Service.Interfaces.QuizzesInterfaces;

import com.example.e_learning_system.Dto.quizzes.QuizSubmissionResponseDTO;
import com.example.e_learning_system.Dto.quizzes.QuizSubmitDTO;
import com.example.e_learning_system.Dto.quizzes.StudentAnswerResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface QuizSubmissions {
    void submitQuiz(QuizSubmitDTO quizSubmitDTO);
    List<QuizSubmissionResponseDTO> getQuizAttempts(Integer quizId ,  Integer userId);
     List<StudentAnswerResponseDTO> getSubmissionAnswers(Integer submissionId);
}

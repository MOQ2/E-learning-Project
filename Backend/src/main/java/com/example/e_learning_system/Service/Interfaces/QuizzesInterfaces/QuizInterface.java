package com.example.e_learning_system.Service.Interfaces.QuizzesInterfaces;

import com.example.e_learning_system.Dto.quizzes.CreateQuizDTO;
import com.example.e_learning_system.Dto.quizzes.QuizResponseDTO;
import com.example.e_learning_system.Dto.quizzes.UpdateQuizDTO;

import java.util.List;

public interface QuizInterface {
     QuizResponseDTO createQuiz(Integer videoId , CreateQuizDTO createQuizDTO);
     QuizResponseDTO  updateQuiz(Integer quizId , UpdateQuizDTO updateQuizDTO);
     List<QuizResponseDTO> getQuizzes(Integer courseId, Integer quizId, String title, Boolean isActive);

}

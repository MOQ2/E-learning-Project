package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.QuizSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmissionEntity, Integer> {
    List<QuizSubmissionEntity> findByQuizId(Integer quizId);
    List<QuizSubmissionEntity> findByQuizIdAndUser_NameContainingIgnoreCase(Integer quizId, String userName);}

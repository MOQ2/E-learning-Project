package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.QuizQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestionEntity, Integer> {
    List<QuizQuestionEntity> findByQuizId(Integer quizId);

}

package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.QuizEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<QuizEntity, Integer> {

}

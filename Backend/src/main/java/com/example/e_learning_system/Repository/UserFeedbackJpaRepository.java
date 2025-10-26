package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFeedbackJpaRepository extends JpaRepository<UserFeedback, Integer> {
    List<UserFeedback> findByCourseIdAndIsActiveTrueOrderByCreatedAtDesc(Integer courseId);
    java.util.Optional<UserFeedback> findByCourseIdAndUserIdAndIsActiveTrue(Integer courseId, Integer userId);
    org.springframework.data.domain.Page<UserFeedback> findAllByCourseIdAndIsActiveTrueOrderByCreatedAtDesc(Integer courseId, org.springframework.data.domain.Pageable pageable);
}

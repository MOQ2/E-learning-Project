package com.example.e_learning_system.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class UserFeedbackRepository {

    @PersistenceContext
    private EntityManager em;

    public Double findAverageRatingByCourseId(Integer courseId) {
        Object result = em.createNativeQuery("SELECT AVG(rating) FROM user_feedback WHERE course_id = :courseId AND is_active = true")
                .setParameter("courseId", courseId)
                .getSingleResult();
        if (result == null) return null;
        if (result instanceof Number) return ((Number) result).doubleValue();
        try {
            return Double.parseDouble(result.toString());
        } catch (Exception e) {
            return null;
        }
    }
    public Integer findReviewCountByCourseId(Integer courseId) {
        Object result = em.createNativeQuery("SELECT COUNT(*) FROM user_feedback WHERE course_id = :courseId AND is_active = true")
                .setParameter("courseId", courseId)
                .getSingleResult();
        if (result == null) return 0;
        if (result instanceof Number) return ((Number) result).intValue();
        try {
            return Integer.parseInt(result.toString());
        } catch (Exception e) {
            return 0;
        }
    }
}


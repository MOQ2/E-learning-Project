package com.example.e_learning_system.Dto.MyCourseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for teacher statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStatsDto {
    // Course statistics
    private Integer totalCourses;
    private Integer publishedCourses;
    private Integer draftCourses;
    private Integer activeCourses;
    
    // Student statistics
    private Integer totalStudents;
    private Integer activeStudents;
    private Integer recentEnrollments30Days;
    
    // Revenue statistics
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    
    // Engagement statistics
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalCompletions;
    private Double averageCompletionRate;
    
    // Content statistics
    private Integer totalLessons;
    private Integer totalModules;
    private Integer totalQuizzes;
}

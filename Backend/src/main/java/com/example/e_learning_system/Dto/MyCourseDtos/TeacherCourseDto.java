package com.example.e_learning_system.Dto.MyCourseDtos;

import com.example.e_learning_system.Config.Category;
import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for teacher-owned courses with statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherCourseDto {
    // Basic course info
    private Integer courseId;
    private String courseName;
    private String description;
    private String thumbnailUrl;
    private Category category;
    private DifficultyLevel difficultyLevel;
    private CourseStatus status;
    
    // Course structure
    private Integer totalModules;
    private Integer totalLessons;
    private Integer totalQuizzes;
    private Integer totalDurationMinutes;
    
    // Pricing information
    private BigDecimal oneTimePrice;
    private BigDecimal subscriptionPriceMonthly;
    private Boolean allowsSubscription;
    private Boolean isFree;
    
    // Statistics
    private Integer totalEnrollments;
    private Integer activeEnrollments;
    private Double averageProgress;
    private Double averageRating;
    private Integer totalReviews;
    private BigDecimal totalRevenue;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastEnrollmentDate;
    
    // Activity indicators
    private Boolean isActive;
    private Boolean isPublished;
    private Integer recentEnrollments30Days;
    private Integer completions;
}

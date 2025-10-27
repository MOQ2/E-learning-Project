package com.example.e_learning_system.Dto.MyLearningDtos;

import com.example.e_learning_system.Config.AccessType;
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
 * DTO for enrolled courses with progress and access information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrolledCourseDto {
    // Basic course info
    private Integer courseId;
    private String courseName;
    private String description;
    private String instructor;
    private String thumbnailUrl;
    private Category category;
    private DifficultyLevel difficultyLevel;
    private CourseStatus status;
    
    // Progress tracking
    private Integer totalModules;
    private Integer completedModules;
    private Integer totalLessons;
    private Integer completedLessons;
    private Integer totalQuizzes;
    private Integer completedQuizzes;
    private Double progressPercentage;
    private Integer totalDurationMinutes;
    private Integer watchedDurationMinutes;
    
    // Access information
    private AccessType accessType;
    private LocalDateTime enrolledDate;
    private LocalDateTime accessUntil;
    private Boolean isActive;
    private Boolean hasLifetimeAccess;
    private Integer daysRemaining;
    
    // Additional info
    private LocalDateTime lastAccessedDate;
    private String currentModule;
    private String currentLesson;
    private Double averageRating;
    private BigDecimal pricePaid;
    
    // Package info (if accessed through package)
    private Integer packageId;
    private String packageName;
}

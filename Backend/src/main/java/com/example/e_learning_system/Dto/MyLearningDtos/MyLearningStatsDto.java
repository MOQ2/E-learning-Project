package com.example.e_learning_system.Dto.MyLearningDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user learning statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyLearningStatsDto {
    private Integer totalEnrolledCourses;
    private Integer activeCourses;
    private Integer completedCourses;
    private Integer totalLessonsCompleted;
    private Integer totalQuizzesCompleted;
    private Integer totalLearningHours;
    private Double averageProgressPercentage;
    private Integer certificatesEarned;
    private Integer currentStreak;
}

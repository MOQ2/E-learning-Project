package com.example.e_learning_system.Dto.MyLearningDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Main response DTO for My Learning page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyLearningResponseDto {
    private MyLearningStatsDto stats;
    private List<EnrolledCourseDto> enrolledCourses;
    private List<EnrolledCourseDto> continueLearning; // Recently accessed courses
    private List<EnrolledCourseDto> upcomingDeadlines; // Courses with expiring access
}

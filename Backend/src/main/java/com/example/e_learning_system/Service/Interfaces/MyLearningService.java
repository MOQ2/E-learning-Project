package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Dto.MyLearningDtos.EnrolledCourseDto;
import com.example.e_learning_system.Dto.MyLearningDtos.MyLearningResponseDto;
import com.example.e_learning_system.Dto.MyLearningDtos.MyLearningStatsDto;

import java.util.List;

/**
 * Service interface for My Learning functionality
 */
public interface MyLearningService {
    
    /**
     * Get complete learning dashboard for a user
     * @param userId User ID
     * @return MyLearningResponseDto with stats and enrolled courses
     */
    MyLearningResponseDto getMyLearningDashboard(Integer userId);
    
    /**
     * Get all enrolled courses for a user with progress
     * @param userId User ID
     * @return List of enrolled courses with progress
     */
    List<EnrolledCourseDto> getEnrolledCourses(Integer userId);
    
    /**
     * Get learning statistics for a user
     * @param userId User ID
     * @return Learning statistics
     */
    MyLearningStatsDto getLearningStats(Integer userId);
    
    /**
     * Get recently accessed courses (Continue Learning)
     * @param userId User ID
     * @param limit Number of courses to return
     * @return List of recently accessed courses
     */
    List<EnrolledCourseDto> getContinueLearning(Integer userId, int limit);
    
    /**
     * Get courses with expiring access
     * @param userId User ID
     * @param daysThreshold Number of days to check for expiration
     * @return List of courses with expiring access
     */
    List<EnrolledCourseDto> getUpcomingDeadlines(Integer userId, int daysThreshold);
    
    /**
     * Get enrolled course details with progress
     * @param userId User ID
     * @param courseId Course ID
     * @return Enrolled course with progress
     */
    EnrolledCourseDto getEnrolledCourseDetails(Integer userId, Integer courseId);
}

package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.ApiResponse;
import com.example.e_learning_system.Dto.MyLearningDtos.EnrolledCourseDto;
import com.example.e_learning_system.Dto.MyLearningDtos.MyLearningResponseDto;
import com.example.e_learning_system.Dto.MyLearningDtos.MyLearningStatsDto;
import com.example.e_learning_system.Service.Interfaces.MyLearningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for My Learning functionality
 */
@RestController
@RequestMapping("/api/my-learning")
@RequiredArgsConstructor
@Slf4j
public class MyLearningController {

    private final MyLearningService myLearningService;


    // TODO use the current user id instead of passing userId in path
    /**
     * Get complete learning dashboard for a user
     * GET /api/my-learning/dashboard/{userId}
     */
    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<ApiResponse<MyLearningResponseDto>> getMyLearningDashboard(
            @PathVariable Integer userId) {
        log.info("Fetching learning dashboard for user {}", userId);
        
        MyLearningResponseDto dashboard = myLearningService.getMyLearningDashboard(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Learning dashboard fetched successfully", dashboard));
    }

    /**
     * Get all enrolled courses for a user
     * GET /api/my-learning/courses/{userId}
     */
    @GetMapping("/courses/{userId}")
    public ResponseEntity<ApiResponse<List<EnrolledCourseDto>>> getEnrolledCourses(
            @PathVariable Integer userId) {
        log.info("Fetching enrolled courses for user {}", userId);
        
        List<EnrolledCourseDto> courses = myLearningService.getEnrolledCourses(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Enrolled courses fetched successfully", courses));
    }

    /**
     * Get learning statistics for a user
     * GET /api/my-learning/stats/{userId}
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<ApiResponse<MyLearningStatsDto>> getLearningStats(
            @PathVariable Integer userId) {
        log.info("Fetching learning stats for user {}", userId);
        
        MyLearningStatsDto stats = myLearningService.getLearningStats(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Learning stats fetched successfully", stats));
    }

    /**
     * Get recently accessed courses (Continue Learning)
     * GET /api/my-learning/continue/{userId}
     */
    @GetMapping("/continue/{userId}")
    public ResponseEntity<ApiResponse<List<EnrolledCourseDto>>> getContinueLearning(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Fetching continue learning courses for user {}", userId);
        
        List<EnrolledCourseDto> courses = myLearningService.getContinueLearning(userId, limit);
        
        return ResponseEntity.ok(
                ApiResponse.success("Continue learning courses fetched successfully", courses));
    }

    /**
     * Get courses with expiring access
     * GET /api/my-learning/deadlines/{userId}
     */
    @GetMapping("/deadlines/{userId}")
    public ResponseEntity<ApiResponse<List<EnrolledCourseDto>>> getUpcomingDeadlines(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "30") int daysThreshold) {
        log.info("Fetching courses with upcoming deadlines for user {}", userId);
        
        List<EnrolledCourseDto> courses = myLearningService.getUpcomingDeadlines(userId, daysThreshold);
        
        return ResponseEntity.ok(
                ApiResponse.success("Courses with upcoming deadlines fetched successfully", courses));
    }

    /**
     * Get enrolled course details with progress
     * GET /api/my-learning/course/{userId}/{courseId}
     */
    @GetMapping("/course/{userId}/{courseId}")
    public ResponseEntity<ApiResponse<EnrolledCourseDto>> getEnrolledCourseDetails(
            @PathVariable Integer userId,
            @PathVariable Integer courseId) {
        log.info("Fetching enrolled course details for user {} and course {}", userId, courseId);
        
        EnrolledCourseDto course = myLearningService.getEnrolledCourseDetails(userId, courseId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Enrolled course details fetched successfully", course));
    }
}

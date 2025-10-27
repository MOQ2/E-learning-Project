package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.ApiResponse;
import com.example.e_learning_system.Dto.MyLearningDtos.EnrolledCourseDto;
import com.example.e_learning_system.Dto.MyLearningDtos.MyLearningResponseDto;
import com.example.e_learning_system.Dto.MyLearningDtos.MyLearningStatsDto;
import com.example.e_learning_system.Service.Interfaces.MyLearningService;
import com.example.e_learning_system.Service.AuthorizationService;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.excpetions.SecurityException;
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
    private final AuthorizationService authorizationService;


    /**
     * Get complete learning dashboard for a user
     * GET /api/my-learning/dashboard/{userId}
     */
    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<ApiResponse<MyLearningResponseDto>> getMyLearningDashboard(
            @PathVariable Integer userId) {
        log.info("Fetching learning dashboard for user {}", userId);
        
        // Users can only access their own dashboard, unless they are admin
        if (!authorizationService.canAccessUserData(userId)) {
            throw SecurityException.accessDenied("User dashboard");
        }
        
        MyLearningResponseDto dashboard = myLearningService.getMyLearningDashboard(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Learning dashboard fetched successfully", dashboard));
    }

    /**
     * Get complete learning dashboard for current user
     * GET /api/my-learning/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<MyLearningResponseDto>> getCurrentUserLearningDashboard() {
        UserEntity currentUser = authorizationService.getCurrentUser();
        log.info("Fetching learning dashboard for current user {}", currentUser.getId());
        
        MyLearningResponseDto dashboard = myLearningService.getMyLearningDashboard(currentUser.getId());
        
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
        
        // Users can only access their own courses, unless they are admin
        if (!authorizationService.canAccessUserData(userId)) {
            throw SecurityException.accessDenied("User courses");
        }
        
        List<EnrolledCourseDto> courses = myLearningService.getEnrolledCourses(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Enrolled courses fetched successfully", courses));
    }

    /**
     * Get all enrolled courses for current user
     * GET /api/my-learning/courses
     */
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<EnrolledCourseDto>>> getCurrentUserEnrolledCourses() {
        UserEntity currentUser = authorizationService.getCurrentUser();
        log.info("Fetching enrolled courses for current user {}", currentUser.getId());
        
        List<EnrolledCourseDto> courses = myLearningService.getEnrolledCourses(currentUser.getId());
        
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
        
        // Users can only access their own stats, unless they are admin
        if (!authorizationService.canAccessUserData(userId)) {
            throw SecurityException.accessDenied("User stats");
        }
        
        MyLearningStatsDto stats = myLearningService.getLearningStats(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Learning stats fetched successfully", stats));
    }

    /**
     * Get learning statistics for current user
     * GET /api/my-learning/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<MyLearningStatsDto>> getCurrentUserLearningStats() {
        UserEntity currentUser = authorizationService.getCurrentUser();
        log.info("Fetching learning stats for current user {}", currentUser.getId());
        
        MyLearningStatsDto stats = myLearningService.getLearningStats(currentUser.getId());
        
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
        
        // Users can only access their own continue learning, unless they are admin
        if (!authorizationService.canAccessUserData(userId)) {
            throw SecurityException.accessDenied("User continue learning");
        }
        
        List<EnrolledCourseDto> courses = myLearningService.getContinueLearning(userId, limit);
        
        return ResponseEntity.ok(
                ApiResponse.success("Continue learning courses fetched successfully", courses));
    }

    /**
     * Get recently accessed courses for current user (Continue Learning)
     * GET /api/my-learning/continue
     */
    @GetMapping("/continue")
    public ResponseEntity<ApiResponse<List<EnrolledCourseDto>>> getCurrentUserContinueLearning(
            @RequestParam(defaultValue = "5") int limit) {
        UserEntity currentUser = authorizationService.getCurrentUser();
        log.info("Fetching continue learning courses for current user {}", currentUser.getId());
        
        List<EnrolledCourseDto> courses = myLearningService.getContinueLearning(currentUser.getId(), limit);
        
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
        
        // Users can only access their own deadlines, unless they are admin
        if (!authorizationService.canAccessUserData(userId)) {
            throw SecurityException.accessDenied("User deadlines");
        }
        
        List<EnrolledCourseDto> courses = myLearningService.getUpcomingDeadlines(userId, daysThreshold);
        
        return ResponseEntity.ok(
                ApiResponse.success("Courses with upcoming deadlines fetched successfully", courses));
    }

    /**
     * Get courses with expiring access for current user
     * GET /api/my-learning/deadlines
     */
    @GetMapping("/deadlines")
    public ResponseEntity<ApiResponse<List<EnrolledCourseDto>>> getCurrentUserUpcomingDeadlines(
            @RequestParam(defaultValue = "30") int daysThreshold) {
        UserEntity currentUser = authorizationService.getCurrentUser();
        log.info("Fetching courses with upcoming deadlines for current user {}", currentUser.getId());
        
        List<EnrolledCourseDto> courses = myLearningService.getUpcomingDeadlines(currentUser.getId(), daysThreshold);
        
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
        
        // Users can only access their own course details, unless they are admin
        if (!authorizationService.canAccessUserData(userId)) {
            throw SecurityException.accessDenied("User course details");
        }
        
        // Additionally check if user can access this specific course
        if (!authorizationService.canAccessCourse(courseId)) {
            throw SecurityException.accessDenied("Course " + courseId);
        }
        
        EnrolledCourseDto course = myLearningService.getEnrolledCourseDetails(userId, courseId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Enrolled course details fetched successfully", course));
    }

    /**
     * Get enrolled course details with progress for current user
     * GET /api/my-learning/course/{courseId}
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<EnrolledCourseDto>> getCurrentUserEnrolledCourseDetails(
            @PathVariable Integer courseId) {
        UserEntity currentUser = authorizationService.getCurrentUser();
        log.info("Fetching enrolled course details for current user {} and course {}", currentUser.getId(), courseId);
        
        // Check if current user can access this course
        if (!authorizationService.canAccessCourse(courseId)) {
            throw SecurityException.accessDenied("Course " + courseId);
        }
        
        EnrolledCourseDto course = myLearningService.getEnrolledCourseDetails(currentUser.getId(), courseId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Enrolled course details fetched successfully", course));
    }
}

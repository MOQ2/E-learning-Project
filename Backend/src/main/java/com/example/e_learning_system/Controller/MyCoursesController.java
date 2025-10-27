package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.ApiResponse;
import com.example.e_learning_system.Dto.MyCourseDtos.MyCoursesResponseDto;
import com.example.e_learning_system.Dto.MyCourseDtos.TeacherCourseDto;
import com.example.e_learning_system.Dto.MyCourseDtos.TeacherStatsDto;
import com.example.e_learning_system.Service.Interfaces.MyCoursesService;
import com.example.e_learning_system.Service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for teacher's My Courses functionality
 */
@RestController
@RequestMapping("/api/my-courses")
@RequiredArgsConstructor
@Slf4j
public class MyCoursesController {

    private final MyCoursesService myCoursesService;
    private final AuthorizationService authorizationService;

    /**
     * Get complete My Courses dashboard for current teacher
     * GET /api/my-courses/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<MyCoursesResponseDto>> getMyCoursesDashboard() {
        // Only teachers and admins can access my courses dashboard
        authorizationService.requireTeacherOrAdmin();
        
        Integer currentUserId = authorizationService.getCurrentUser().getId();
        log.info("Fetching My Courses dashboard for teacher {}", currentUserId);
        
        MyCoursesResponseDto dashboard = myCoursesService.getMyCoursesDashboard(currentUserId);
        
        return ResponseEntity.ok(
                ApiResponse.success("My Courses dashboard fetched successfully", dashboard));
    }

    /**
     * Get all courses created by current teacher
     * GET /api/my-courses/courses
     */
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<TeacherCourseDto>>> getTeacherCourses() {
        // Only teachers and admins can access teacher courses
        authorizationService.requireTeacherOrAdmin();
        
        Integer currentUserId = authorizationService.getCurrentUser().getId();
        log.info("Fetching courses for teacher {}", currentUserId);
        
        List<TeacherCourseDto> courses = myCoursesService.getTeacherCourses(currentUserId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Teacher courses fetched successfully", courses));
    }

    /**
     * Get teacher statistics for current teacher
     * GET /api/my-courses/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<TeacherStatsDto>> getTeacherStats() {
        // Only teachers and admins can access teacher statistics
        authorizationService.requireTeacherOrAdmin();
        
        Integer currentUserId = authorizationService.getCurrentUser().getId();
        log.info("Fetching statistics for teacher {}", currentUserId);
        
        TeacherStatsDto stats = myCoursesService.getTeacherStats(currentUserId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Teacher statistics fetched successfully", stats));
    }

    /**
     * Get recently updated courses for current teacher
     * GET /api/my-courses/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<TeacherCourseDto>>> getRecentlyUpdatedCourses(
            @RequestParam(defaultValue = "5") int limit) {
        // Only teachers and admins can access recently updated courses
        authorizationService.requireTeacherOrAdmin();
        
        Integer currentUserId = authorizationService.getCurrentUser().getId();
        log.info("Fetching recently updated courses for teacher {}", currentUserId);
        
        List<TeacherCourseDto> courses = myCoursesService.getRecentlyUpdatedCourses(currentUserId, limit);
        
        return ResponseEntity.ok(
                ApiResponse.success("Recently updated courses fetched successfully", courses));
    }

    /**
     * Get top performing courses for current teacher
     * GET /api/my-courses/top
     */
    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<TeacherCourseDto>>> getTopPerformingCourses(
            @RequestParam(defaultValue = "5") int limit) {
        // Only teachers and admins can access top performing courses
        authorizationService.requireTeacherOrAdmin();
        
        Integer currentUserId = authorizationService.getCurrentUser().getId();
        log.info("Fetching top performing courses for teacher {}", currentUserId);
        
        List<TeacherCourseDto> courses = myCoursesService.getTopPerformingCourses(currentUserId, limit);
        
        return ResponseEntity.ok(
                ApiResponse.success("Top performing courses fetched successfully", courses));
    }

    /**
     * Get course details with statistics for current teacher
     * GET /api/my-courses/course/{courseId}
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<TeacherCourseDto>> getCourseDetails(
            @PathVariable Integer courseId) {
        // Check if current user can access this course (creator or admin)
        authorizationService.requireCourseEditAccess(courseId);
        
        Integer currentUserId = authorizationService.getCurrentUser().getId();
        log.info("Fetching course details for teacher {} and course {}", currentUserId, courseId);
        
        TeacherCourseDto course = myCoursesService.getCourseDetails(currentUserId, courseId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Course details fetched successfully", course));
    }
}

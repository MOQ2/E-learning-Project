package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.ApiResponse;
import com.example.e_learning_system.Dto.CourseDtos.CourseDetailsDto;
import com.example.e_learning_system.Dto.MyLearningDtos.MyLearningResponseDto;
import com.example.e_learning_system.Service.AuthorizationService;
import com.example.e_learning_system.Service.Interfaces.CourseService;
import com.example.e_learning_system.Service.Interfaces.MyLearningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for student learning functionality
 * Students can view their enrolled courses but NOT lesson content
 */
@RestController
@RequestMapping("/api/student-learning")
@RequiredArgsConstructor
@Slf4j
public class StudentLearningController {

    private final MyLearningService myLearningService;
    private final CourseService courseService;
    private final AuthorizationService authorizationService;

    /**
     * Get my learning dashboard for current student
     * Students can only access their own learning data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<MyLearningResponseDto>> getMyLearningDashboard() {
        Integer currentUserId = authorizationService.getCurrentUser().getId();
        log.info("Fetching My Learning dashboard for student {}", currentUserId);
        
        MyLearningResponseDto dashboard = myLearningService.getMyLearningDashboard(currentUserId);
        
        return ResponseEntity.ok(
                ApiResponse.success("My Learning dashboard fetched successfully", dashboard));
    }

    /**
     * Get enrolled course details for student
     * Students can view course details if they have valid enrollment but NOT lesson content
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<CourseDetailsDto>> getEnrolledCourseDetails(
            @PathVariable Integer courseId) {
        
        Integer currentUserId = authorizationService.getCurrentUser().getId();
        
        // Check if student has valid access to this course
        authorizationService.requireCourseAccess(courseId);
        
        log.info("Student {} accessing enrolled course {}", currentUserId, courseId);
        
        // Get course details - students can see course overview but not lesson content
        CourseDetailsDto course = courseService.getCourseById(courseId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Enrolled course details retrieved successfully", course));
    }

    /**
     * Check if current student has access to a course
     */
    /**
     * Check if current student has access to a course
     */
    @GetMapping("/courses/{courseId}/access-check")
    public ResponseEntity<ApiResponse<Boolean>> checkCourseAccess(@PathVariable Integer courseId) {
        boolean hasAccess = authorizationService.canAccessCourse(courseId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Course access check completed", hasAccess));
    }
}
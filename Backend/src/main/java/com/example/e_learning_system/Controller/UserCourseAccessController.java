package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Config.AccessType;
import com.example.e_learning_system.Dto.UserCourseAccessResponseDTO;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Service.Interfaces.UserCourseAccessService;
import com.example.e_learning_system.Service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user-course-access")
public class UserCourseAccessController {

    @Autowired
    private UserCourseAccessService userCourseAccessService;
    
    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Grant course access - Only admins can grant access
     */
    @PostMapping("/grant-course-access")
    public ResponseEntity<UserCourseAccessResponseDTO> grantCourseAccess(
            @RequestParam Integer userId,
            @RequestParam Integer courseId,
            @RequestParam AccessType accessType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime accessUntil,
            @RequestParam(required = false) Integer paymentId) {
        
        // Only admins can grant course access
        authorizationService.requireAdmin();
        
        UserCourseAccessResponseDTO response = userCourseAccessService.grantCourseAccess(
                userId, courseId, accessType, accessUntil, paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Grant package access - Only admins can grant access
     */
    @PostMapping("/grant-package-access")
    public ResponseEntity<UserCourseAccessResponseDTO> grantPackageAccess(
            @RequestParam Integer userId,
            @RequestParam Integer packageId,
            @RequestParam AccessType accessType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime accessUntil,
            @RequestParam(required = false) Integer paymentId) {
        
        // Only admins can grant package access
        authorizationService.requireAdmin();
        
        UserCourseAccessResponseDTO response = userCourseAccessService.grantPackageAccess(
                userId, packageId, accessType, accessUntil, paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user has valid course access - Accessible by the user themselves or admin
     */
    @GetMapping("/check-access/{userId}/{courseId}")
    public ResponseEntity<Boolean> hasValidAccess(
            @PathVariable Integer userId,
            @PathVariable Integer courseId) {
        
        // Only the user themselves or admin can check access
        authorizationService.requireUserDataAccess(userId);
        
        boolean hasAccess = userCourseAccessService.hasValidAccess(userId, courseId);
        return ResponseEntity.ok(hasAccess);
    }

    /**
     * Get user course access details - Accessible by the user themselves or admin
     */
    @GetMapping("/get-access/{userId}/{courseId}")
    public ResponseEntity<UserCourseAccessResponseDTO> getUserCourseAccess(
            @PathVariable Integer userId,
            @PathVariable Integer courseId) {
        
        // Only the user themselves or admin can get access details
        authorizationService.requireUserDataAccess(userId);
        
        UserCourseAccessResponseDTO response = userCourseAccessService.getUserCourseAccess(userId, courseId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user's active accesses - Accessible by the user themselves or admin
     */
    @GetMapping("/user-active-accesses/{userId}")
    public ResponseEntity<List<UserCourseAccessResponseDTO>> getUserActiveAccesses(
            @PathVariable Integer userId) {
        
        // Only the user themselves or admin can get active accesses
        authorizationService.requireUserDataAccess(userId);
        
        List<UserCourseAccessResponseDTO> accesses = userCourseAccessService.getUserActiveAccesses(userId);
        return ResponseEntity.ok(accesses);
    }

    @GetMapping("/user-accessible-courses/{userId}")
    public ResponseEntity<List<Course>> getUserAccessibleCourses(
            @PathVariable Integer userId) {
        
        List<Course> courses = userCourseAccessService.getUserAccessibleCourses(userId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/course-active-accesses/{courseId}")
    public ResponseEntity<List<UserCourseAccessResponseDTO>> getCourseActiveAccesses(
            @PathVariable Integer courseId) {
        
        List<UserCourseAccessResponseDTO> accesses = userCourseAccessService.getCourseActiveAccesses(courseId);
        return ResponseEntity.ok(accesses);
    }

    @DeleteMapping("/revoke-course-access/{userId}/{courseId}")
    public ResponseEntity<Void> revokeCourseAccess(
            @PathVariable Integer userId,
            @PathVariable Integer courseId) {
        
        userCourseAccessService.revokeCourseAccess(userId, courseId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/revoke-package-access/{userId}/{packageId}")
    public ResponseEntity<Void> revokePackageAccess(
            @PathVariable Integer userId,
            @PathVariable Integer packageId) {
        
        userCourseAccessService.revokePackageAccess(userId, packageId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/extend-access/{accessId}")
    public ResponseEntity<UserCourseAccessResponseDTO> extendAccess(
            @PathVariable Integer accessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newAccessUntil) {
        
        UserCourseAccessResponseDTO response = userCourseAccessService.extendAccess(accessId, newAccessUntil);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process-expired-accesses")
    public ResponseEntity<Void> processExpiredAccesses() {
        userCourseAccessService.processExpiredAccesses();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/accesses-by-type")
    public ResponseEntity<List<UserCourseAccessResponseDTO>> getAccessesByType(
            @RequestParam AccessType accessType) {
        
        List<UserCourseAccessResponseDTO> accesses = userCourseAccessService.getAccessesByType(accessType);
        return ResponseEntity.ok(accesses);
    }

    @GetMapping("/has-package-access/{userId}/{courseId}")
    public ResponseEntity<Boolean> hasAccessThroughPackage(
            @PathVariable Integer userId,
            @PathVariable Integer courseId) {
        
        boolean hasPackageAccess = userCourseAccessService.hasAccessThroughPackage(userId, courseId);
        return ResponseEntity.ok(hasPackageAccess);
    }
}

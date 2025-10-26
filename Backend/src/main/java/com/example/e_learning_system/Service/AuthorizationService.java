package com.example.e_learning_system.Service;

import com.example.e_learning_system.Config.RolesName;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Repository.CourseRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.Security.UserUtil;
import com.example.e_learning_system.Service.Interfaces.UserCourseAccessService;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import com.example.e_learning_system.excpetions.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseAccessService userCourseAccessService;

    /**
     * Get current authenticated user
     */
    public UserEntity getCurrentUser() {
        Long userId = UserUtil.getCurrentUserId();
        if (userId == null) {
            throw SecurityException.tokenMissing();
        }
        
        return userRepository.findById(userId.intValue())
                .orElseThrow(() -> ResourceNotFound.userNotFound("User ID: " + userId));
    }

    /**
     * Check if current user is admin
     */
    public boolean isCurrentUserAdmin() {
        try {
            UserEntity user = getCurrentUser();
            return user.getRole().getName() == RolesName.ADMIN;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if current user is teacher
     */
    public boolean isCurrentUserTeacher() {
        try {
            UserEntity user = getCurrentUser();
            return user.getRole().getName() == RolesName.TEACHER;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if current user is student
     */
    public boolean isCurrentUserStudent() {
        try {
            UserEntity user = getCurrentUser();
            return user.getRole().getName() == RolesName.USER; // USER role is student
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ensure current user is admin or throw exception
     */
    public void requireAdmin() {
        if (!isCurrentUserAdmin()) {
            throw SecurityException.roleNotAuthorized(getCurrentUser().getRole().getName().toString(), "admin operations");
        }
    }

    /**
     * Ensure current user is teacher or throw exception
     */
    public void requireTeacher() {
        if (!isCurrentUserTeacher()) {
            throw SecurityException.roleNotAuthorized(getCurrentUser().getRole().getName().toString(), "teacher operations");
        }
    }

    /**
     * Ensure current user is teacher or admin or throw exception
     */
    public void requireTeacherOrAdmin() {
        if (!isCurrentUserTeacher() && !isCurrentUserAdmin()) {
            throw SecurityException.roleNotAuthorized(getCurrentUser().getRole().getName().toString(), "teacher or admin operations");
        }
    }

    /**
     * Check if current user can access course (enrolled student with valid access, or course creator, or admin)
     */
    public boolean canAccessCourse(Integer courseId) {
        UserEntity currentUser = getCurrentUser();
        
        // Admin can access any course
        if (currentUser.getRole().getName() == RolesName.ADMIN) {
            return true;
        }
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId.toString()));
        
        // Teacher can access their own courses
        if (currentUser.getRole().getName() == RolesName.TEACHER && 
            course.getCreatedBy().getId() == currentUser.getId()) {
            return true;
        }
        
        // Student can access if they have valid enrollment
        if (currentUser.getRole().getName() == RolesName.USER) {
            return userCourseAccessService.hasValidAccess(currentUser.getId(), courseId);
        }
        
        return false;
    }

    /**
     * Check if current user can access course lessons (enrolled student with valid access, or course creator, or admin)
     */
    public boolean canAccessCourseLessons(Integer courseId) {
        UserEntity currentUser = getCurrentUser();
        
        // Admin can access any course lessons
        if (currentUser.getRole().getName() == RolesName.ADMIN) {
            return true;
        }
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId.toString()));
        
        // Teacher can access lessons of their own courses
        if (currentUser.getRole().getName() == RolesName.TEACHER && 
            course.getCreatedBy().getId() == currentUser.getId()) {
            return true;
        }
        
        // Students cannot access lesson content even if enrolled (as per requirements)
        return false;
    }

    /**
     * Check if current user can edit course (course creator or admin)
     */
    public boolean canEditCourse(Integer courseId) {
        UserEntity currentUser = getCurrentUser();
        
        // Admin can edit any course
        if (currentUser.getRole().getName() == RolesName.ADMIN) {
            return true;
        }
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId.toString()));
        
        // Teacher can edit their own courses
        return currentUser.getRole().getName() == RolesName.TEACHER && 
               course.getCreatedBy().getId() == currentUser.getId();
    }

    /**
     * Require current user can access course
     */
    public void requireCourseAccess(Integer courseId) {
        if (!canAccessCourse(courseId)) {
            throw SecurityException.accessDenied("course " + courseId);
        }
    }

    /**
     * Require current user can access course lessons
     */
    public void requireCourseLessonAccess(Integer courseId) {
        if (!canAccessCourseLessons(courseId)) {
            throw SecurityException.accessDenied("course lessons for course " + courseId);
        }
    }

    /**
     * Require current user can edit course
     */
    public void requireCourseEditAccess(Integer courseId) {
        if (!canEditCourse(courseId)) {
            throw SecurityException.accessDenied("edit course " + courseId);
        }
    }

    /**
     * Check if current user can download attachments (only teachers and admins)
     */
    public boolean canDownloadAttachments() {
        return true;
    }

    /**
     * Require current user can download attachments
     */
    public void requireAttachmentDownloadAccess() {
        if (!canDownloadAttachments()) {
            throw SecurityException.accessDenied("download attachments");
        }
    }

    /**
     * Check if current user can view videos (only teachers and admins)
     */
    public boolean canViewVideos() {
        return isCurrentUserTeacher() || isCurrentUserAdmin();
    }

    /**
     * Require current user can view videos
     */
    public void requireVideoViewAccess() {
        if (!canViewVideos()) {
            throw SecurityException.accessDenied("view videos");
        }
    }

    /**
     * Check if user matches current user or current user is admin
     */
    public boolean canAccessUserData(Integer userId) {
        UserEntity currentUser = getCurrentUser();
        return currentUser.getRole().getName() == RolesName.ADMIN || 
               currentUser.getId() == userId;
    }

    /**
     * Require user matches current user or current user is admin
     */
    public void requireUserDataAccess(Integer userId) {
        if (!canAccessUserData(userId)) {
            throw SecurityException.accessDenied("user data for user " + userId);
        }
    }
}
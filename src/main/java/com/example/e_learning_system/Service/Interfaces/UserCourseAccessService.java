package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Config.AccessType;
import com.example.e_learning_system.Dto.UserCourseAccessResponseDTO;
import com.example.e_learning_system.Entities.Course;

import java.time.LocalDateTime;
import java.util.List;

public interface UserCourseAccessService {
    
    UserCourseAccessResponseDTO grantCourseAccess(Integer userId, Integer courseId, AccessType accessType, LocalDateTime accessUntil, Integer paymentId);
    
    UserCourseAccessResponseDTO grantPackageAccess(Integer userId, Integer packageId, AccessType accessType, LocalDateTime accessUntil, Integer paymentId);
    
    boolean hasValidAccess(Integer userId, Integer courseId);
    
    UserCourseAccessResponseDTO getUserCourseAccess(Integer userId, Integer courseId);
    
    List<UserCourseAccessResponseDTO> getUserActiveAccesses(Integer userId);
    
    List<Course> getUserAccessibleCourses(Integer userId);
    
    List<UserCourseAccessResponseDTO> getCourseActiveAccesses(Integer courseId);
    
    void revokeCourseAccess(Integer userId, Integer courseId);
    
    void revokePackageAccess(Integer userId, Integer packageId);
    
    UserCourseAccessResponseDTO extendAccess(Integer accessId, LocalDateTime newAccessUntil);
    
    void processExpiredAccesses();
    
    List<UserCourseAccessResponseDTO> getAccessesByType(AccessType accessType);
    
    boolean hasAccessThroughPackage(Integer userId, Integer courseId);
}

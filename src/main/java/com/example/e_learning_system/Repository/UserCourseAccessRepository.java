package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Config.AccessType;
import com.example.e_learning_system.Entities.UserCourseAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCourseAccessRepository extends JpaRepository<UserCourseAccess, Integer> {

    List<UserCourseAccess> findByUserIdAndIsActiveTrue(Integer userId);

    List<UserCourseAccess> findByCourseIdAndIsActiveTrue(Integer courseId);

    @Query("SELECT uca FROM UserCourseAccess uca WHERE uca.user.id = :userId AND uca.course.id = :courseId AND uca.isActive = true")
    Optional<UserCourseAccess> findActiveAccessByUserAndCourse(@Param("userId") Integer userId, @Param("courseId") Integer courseId);

    @Query("SELECT uca FROM UserCourseAccess uca WHERE uca.user.id = :userId AND uca.course.id = :courseId AND " +
           "uca.isActive = true AND (uca.accessUntil IS NULL OR uca.accessUntil > :now)")
    Optional<UserCourseAccess> findValidAccessByUserAndCourse(@Param("userId") Integer userId, 
                                                              @Param("courseId") Integer courseId, 
                                                              @Param("now") LocalDateTime now);

    @Query("SELECT uca FROM UserCourseAccess uca WHERE uca.user.id = :userId AND uca.packageEntity.id = :packageId AND uca.isActive = true")
    List<UserCourseAccess> findActiveAccessByUserAndPackage(@Param("userId") Integer userId, @Param("packageId") Integer packageId);

    @Query("SELECT uca FROM UserCourseAccess uca WHERE uca.accessType = :accessType AND uca.isActive = true")
    List<UserCourseAccess> findByAccessTypeAndIsActiveTrue(@Param("accessType") AccessType accessType);

    @Query("SELECT uca FROM UserCourseAccess uca WHERE uca.accessUntil IS NOT NULL AND uca.accessUntil < :now AND uca.isActive = true")
    List<UserCourseAccess> findExpiredAccesses(@Param("now") LocalDateTime now);

    @Query("SELECT uca FROM UserCourseAccess uca WHERE uca.user.id = :userId AND " +
           "uca.isActive = true AND (uca.accessUntil IS NULL OR uca.accessUntil > :now)")
    List<UserCourseAccess> findActiveAccessesByUser(@Param("userId") Integer userId, @Param("now") LocalDateTime now);

    @Query("SELECT DISTINCT uca.course FROM UserCourseAccess uca WHERE uca.user.id = :userId AND " +
           "uca.isActive = true AND (uca.accessUntil IS NULL OR uca.accessUntil > :now)")
    List<com.example.e_learning_system.Entities.Course> findAccessibleCoursesByUser(@Param("userId") Integer userId, @Param("now") LocalDateTime now);

    boolean existsByUserIdAndCourseIdAndIsActiveTrue(Integer userId, Integer courseId);
}

package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Entities.Module;
import com.example.e_learning_system.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer> {


    // Find by status
    List<Module> findByCourseStatus(CourseStatus courseStatus);

    // Find by active status
    List<Module> findByIsActive(boolean isActive);

    // Find by creator
    List<Module> findByCreatedBy(UserEntity createdBy);

    // Find by name containing (case insensitive)
    List<Module> findByNameContainingIgnoreCase(String name);

    // Find active modules by status
    List<Module> findByIsActiveAndCourseStatus(boolean isActive, CourseStatus courseStatus);

    // Find by creator and status
    List<Module> findByCreatedByAndCourseStatus(UserEntity createdBy, CourseStatus courseStatus);

    // Find modules by estimated duration range
    List<Module> findByEstimatedDurationBetween(int minDuration, int maxDuration);

    // Custom query for published active modules
    @Query("SELECT m FROM Module m WHERE m.courseStatus = 'PUBLISHED' AND m.isActive = true")
    List<Module> findPublishedAndActiveModules();

    // Find modules by creator and active status
    List<Module> findByCreatedByAndIsActive(UserEntity createdBy, boolean isActive);
}
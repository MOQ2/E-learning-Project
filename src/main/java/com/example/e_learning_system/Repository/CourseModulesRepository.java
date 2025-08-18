package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.CourseModules;
import com.example.e_learning_system.Entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseModulesRepository extends JpaRepository<CourseModules, Integer> {

    // Find by course
    List<CourseModules> findByCourse(Course course);

    // Find by module
    List<CourseModules> findByModule(Module module);

    // Find by course and active status
    List<CourseModules> findByCourseAndIsActive(Course course, boolean isActive);

    // Find by course ordered by module order
    List<CourseModules> findByCourseOrderByModuleOrderAsc(Course course);

    // Find active modules for a course ordered by module order
    List<CourseModules> findByCourseAndIsActiveOrderByModuleOrderAsc(Course course, boolean isActive);

    // Find by course and module order
    Optional<CourseModules> findByCourseAndModuleOrder(Course course, Integer moduleOrder);

    // Check if module order exists for a course
    boolean existsByCourseAndModuleOrder(Course course, Integer moduleOrder);

    // Get maximum module order for a course
    @Query("SELECT MAX(cm.moduleOrder) FROM CourseModules cm WHERE cm.course = :course")
    Optional<Integer> findMaxModuleOrderByCourse(@Param("course") Course course);

    // Find active status
    List<CourseModules> findByIsActive(boolean isActive);
}

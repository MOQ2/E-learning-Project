package com.example.e_learning_system.Repository;




import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.DifficultyLevel;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    // Find by status
    List<Course> findByStatus(CourseStatus status);

    // Find by active status
    List<Course> findByIsActive(boolean isActive);

    // Find by free status
    List<Course> findByIsFree(boolean isFree);

    // Find by creator
    List<Course> findByCreatedBy(UserEntity createdBy);

    // Find by difficulty level
    List<Course> findByDifficultyLevel(DifficultyLevel difficultyLevel);


    // Find courses by name containing (case insensitive)
    List<Course> findByNameContainingIgnoreCase(String name);

    // Find active courses by status
    List<Course> findByIsActiveAndStatus(boolean isActive, CourseStatus status);

    // Find courses by price range
    List<Course> findByOneTimePriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Find free and active courses
    List<Course> findByIsFreeAndIsActive(boolean isFree, boolean isActive);

    // Custom query for published active courses
    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND c.isActive = true")
    List<Course> findPublishedAndActiveCourses();

    // Find courses by creator and status
    List<Course> findByCreatedByAndStatus(UserEntity createdBy, CourseStatus status);

    // Search courses by query (exact match or like in title, description, tags, category)
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN c.tags t " +
           "WHERE c.isActive = true AND c.status = 'PUBLISHED' AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CAST(c.category AS string)) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Course> searchCourses(@org.springframework.data.repository.query.Param("query") String query);
}

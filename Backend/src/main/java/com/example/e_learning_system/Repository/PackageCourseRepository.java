package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.PackageCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageCourseRepository extends JpaRepository<PackageCourse, Integer> {

    List<PackageCourse> findByPackageEntityId(Integer packageId);

    List<PackageCourse> findByCourseId(Integer courseId);

    @Query("SELECT pc FROM PackageCourse pc WHERE pc.packageEntity.id = :packageId AND pc.course.id = :courseId")
    Optional<PackageCourse> findByPackageIdAndCourseId(@Param("packageId") Integer packageId, 
                                                       @Param("courseId") Integer courseId);

    @Query("SELECT pc.course FROM PackageCourse pc WHERE pc.packageEntity.id = :packageId")
    List<com.example.e_learning_system.Entities.Course> findCoursesByPackageId(@Param("packageId") Integer packageId);

    @Query("SELECT pc.packageEntity FROM PackageCourse pc WHERE pc.course.id = :courseId")
    List<com.example.e_learning_system.Entities.Package> findPackagesByCourseId(@Param("courseId") Integer courseId);

    void deleteByPackageEntityIdAndCourseId(Integer packageId, Integer courseId);

    boolean existsByPackageEntityIdAndCourseId(Integer packageId, Integer courseId);
    
    @Query("SELECT COUNT(pc) FROM PackageCourse pc WHERE pc.packageEntity.id = :packageId")
    int countCoursesByPackageId(@Param("packageId") Integer packageId);
}

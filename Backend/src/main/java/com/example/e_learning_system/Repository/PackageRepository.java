package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Integer> {

    List<Package> findByIsActiveTrueOrderByCreatedAtDesc();

    List<Package> findByIsActiveTrueAndNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Package p WHERE p.isActive = true AND p.id = :id")
    Optional<Package> findActivePackageById(@Param("id") Integer id);

    @Query("SELECT p FROM Package p WHERE p.isActive = true AND " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Package> findActivePackagesWithFilters(@Param("name") String name,
                                               @Param("minPrice") java.math.BigDecimal minPrice,
                                               @Param("maxPrice") java.math.BigDecimal maxPrice);

    @Query("SELECT COUNT(pc) FROM PackageCourse pc WHERE pc.packageEntity.id = :packageId")
    Long countCoursesByPackageId(@Param("packageId") Integer packageId);
}

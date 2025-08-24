package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Config.SimplePaymentStatus;
import com.example.e_learning_system.Config.SimplePaymentType;
import com.example.e_learning_system.Entities.SimplePayment;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SimplePaymentRepository extends JpaRepository<SimplePayment, Integer> {

    List<SimplePayment> findByUserIdOrderByCreatedAtDesc(Integer userId);

    List<SimplePayment> findByStatusOrderByCreatedAtDesc(SimplePaymentStatus status);

    List<SimplePayment> findByPaymentTypeAndStatusOrderByCreatedAtDesc(SimplePaymentType paymentType, SimplePaymentStatus status);

    Optional<SimplePayment> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<SimplePayment> findByStripeSessionId(String stripeSessionId);

    @Query("SELECT p FROM SimplePayment p WHERE p.user.id = :userId AND p.status = :status")
    List<SimplePayment> findByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") SimplePaymentStatus status);

    @Query("SELECT p FROM SimplePayment p WHERE p.course.id = :courseId AND p.status = 'COMPLETED'")
    List<SimplePayment> findSuccessfulPaymentsByCourse(@Param("courseId") Integer courseId);

    @Query("SELECT p FROM SimplePayment p WHERE p.packageEntity.id = :packageId AND p.status = 'COMPLETED'")
    List<SimplePayment> findSuccessfulPaymentsByPackage(@Param("packageId") Integer packageId);

    @Query("SELECT p FROM SimplePayment p WHERE p.user.id = :userId AND " +
           "((p.course.id = :courseId AND p.packageEntity IS NULL) OR " +
           "(p.packageEntity.id = :packageId AND p.course IS NULL)) AND " +
           "p.status = 'COMPLETED'")
    List<SimplePayment> findUserPaymentsForCourseOrPackage(@Param("userId") Integer userId,
                                                           @Param("courseId") Integer courseId,
                                                           @Param("packageId") Integer packageId);

    @Query("SELECT p FROM SimplePayment p WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.status = 'COMPLETED'")
    List<SimplePayment> findSuccessfulPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(p.finalAmount) FROM SimplePayment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalRevenueBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    // Additional methods needed by the service
    List<SimplePayment> findByUserOrderByPaymentDateDesc(UserEntity user);
    
    List<SimplePayment> findByStatusOrderByPaymentDateDesc(SimplePaymentStatus status);
    
    List<SimplePayment> findByPaymentTypeOrderByPaymentDateDesc(SimplePaymentType paymentType);
    
    List<SimplePayment> findByPaymentDateBetweenOrderByPaymentDateDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT SUM(p.finalAmount) FROM SimplePayment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    List<SimplePayment> findByCourseAndStatusOrderByPaymentDateDesc(Course course, SimplePaymentStatus status);
    
    List<SimplePayment> findByPackageEntityAndStatusOrderByPaymentDateDesc(Package packageEntity, SimplePaymentStatus status);
    
    boolean existsByUserIdAndCourseIdAndStatus(Integer userId, Integer courseId, SimplePaymentStatus status);
    
    boolean existsByUserIdAndPackageEntityIdAndStatus(Integer userId, Integer packageId, SimplePaymentStatus status);
}

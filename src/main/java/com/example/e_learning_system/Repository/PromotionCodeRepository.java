package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.PromotionCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionCodeRepository extends JpaRepository<PromotionCode, Integer> {

    Optional<PromotionCode> findByCode(String code);
    
    Optional<PromotionCode> findByCodeAndIsActiveTrue(String code);

    List<PromotionCode> findByIsActiveTrueOrderByCreatedAtDesc();
    
    @Query("SELECT p FROM PromotionCode p WHERE p.isActive = true AND " +
           "(p.validFrom IS NULL OR p.validFrom <= :now) AND " +
           "(p.validUntil IS NULL OR p.validUntil >= :now) AND " +
           "(p.maxUses IS NULL OR p.currentUses < p.maxUses)")
    List<PromotionCode> findActivePromotionCodes(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM PromotionCode p WHERE p.isActive = true AND " +
           "p.code = :code AND " +
           "(p.validFrom IS NULL OR p.validFrom <= :now) AND " +
           "(p.validUntil IS NULL OR p.validUntil >= :now) AND " +
           "(p.maxUses IS NULL OR p.currentUses < p.maxUses)")
    Optional<PromotionCode> findValidPromotionCode(@Param("code") String code, @Param("now") LocalDateTime now);

    @Query("SELECT p FROM PromotionCode p WHERE p.isActive = true AND " +
           "(:applicableToCourses = true AND p.applicableToCourses = true) OR " +
           "(:applicableToPackages = true AND p.applicableToPackages = true)")
    List<PromotionCode> findActivePromotionCodesForType(@Param("applicableToCourses") boolean applicableToCourses,
                                                        @Param("applicableToPackages") boolean applicableToPackages);

    @Query("SELECT p FROM PromotionCode p WHERE " +
           "(:code IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :code, '%'))) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive)")
    List<PromotionCode> findPromotionCodesWithFilters(@Param("code") String code,
                                                      @Param("isActive") Boolean isActive);
                                                      
    List<PromotionCode> findByCodeContainingIgnoreCaseAndIsActive(String code, Boolean isActive);

    List<PromotionCode> findByValidUntilBeforeAndIsActiveTrue(LocalDateTime dateTime);
    
    @Query("SELECT p FROM PromotionCode p WHERE p.isActive = true AND p.validUntil < :now")
    List<PromotionCode> findExpiredPromotionCodes(@Param("now") LocalDateTime now);
}

package com.example.e_learning_system.Entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "promotion_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PromotionCode extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_percentage", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "current_uses")
    @Builder.Default
    private Integer currentUses = 0;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "applicable_to_courses")
    @Builder.Default
    private Boolean applicableToCourses = true;

    @Column(name = "applicable_to_packages")
    @Builder.Default
    private Boolean applicableToPackages = true;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // One-to-many relationship with SimplePayment
    @OneToMany(mappedBy = "promotionCode", fetch = FetchType.LAZY)
    private List<SimplePayment> payments;
}

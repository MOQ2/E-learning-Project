package com.example.e_learning_system.Entities;

import com.example.e_learning_system.Config.SimplePaymentStatus;
import com.example.e_learning_system.Config.SimplePaymentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "simple_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SimplePayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private Package packageEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private SimplePaymentType paymentType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_code_id")
    private PromotionCode promotionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SimplePaymentStatus status = SimplePaymentStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "subscription_duration_months")
    private Integer subscriptionDurationMonths;

    @Column(name = "stripe_payment_intent_id", length = 255)
    private String stripePaymentIntentId;

    @Column(name = "stripe_session_id", length = 255)
    private String stripeSessionId;

    // One-to-many relationship with UserCourseAccess
    @OneToMany(mappedBy = "payment", fetch = FetchType.LAZY)
    private List<UserCourseAccess> userCourseAccesses;
}

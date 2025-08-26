package com.example.e_learning_system.Entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Package extends BaseEntity {

    @Column(name = "name", nullable = false, length = 250)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "subscription_price_monthly", precision = 10, scale = 2)
    private BigDecimal subscriptionPriceMonthly;

    @Column(name = "subscription_price_3_months", precision = 10, scale = 2)
    private BigDecimal subscriptionPrice3Months;

    @Column(name = "subscription_price_6_months", precision = 10, scale = 2)
    private BigDecimal subscriptionPrice6Months;

    @Column(name = "allows_subscription")
    @Builder.Default
    private Boolean allowsSubscription = false;


    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // One-to-many relationship with PackageCourse
    @OneToMany(mappedBy = "packageEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PackageCourse> packageCourses;

    // One-to-many relationship with SimplePayment
    @OneToMany(mappedBy = "packageEntity", fetch = FetchType.LAZY)
    private List<SimplePayment> payments;
}

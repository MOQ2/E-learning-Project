package com.example.e_learning_system.Entities;

import com.example.e_learning_system.Config.AccessType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_course_access")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserCourseAccess extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private Package packageEntity;
    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false)
        @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AccessType accessType;

    @Column(name = "access_until")
    private LocalDateTime accessUntil;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private SimplePayment payment;
}

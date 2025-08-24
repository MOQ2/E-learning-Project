package com.example.e_learning_system.Dto;

import com.example.e_learning_system.Config.SimplePaymentStatus;
import com.example.e_learning_system.Config.SimplePaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimplePaymentResponseDTO {
    private Integer id;
    private Integer userId;
    private String userEmail;
    private Integer courseId;
    private String courseName;
    private Integer packageId;
    private String packageName;
    private SimplePaymentType paymentType;
    private BigDecimal amount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String promotionCode;
    private SimplePaymentStatus status;
    private LocalDateTime paymentDate;
    private Integer subscriptionDurationMonths;
    private String stripePaymentIntentId;
    private String stripeSessionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.example.e_learning_system.Dto;

import com.example.e_learning_system.Config.SimplePaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimplePaymentRequestDTO {
    
    @NotNull(message = "User ID is required")
    private Integer userId;
    
    private Integer courseId;
    
    private Integer packageId;
    
    @NotNull(message = "Payment type is required")
    private SimplePaymentType paymentType;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    private String promotionCode;
    
    @Min(value = 1, message = "Subscription duration must be at least 1 month")
    private Integer subscriptionDurationMonths;
}

package com.example.e_learning_system.Dto.PurchaseDtos;

import com.example.e_learning_system.Config.SimplePaymentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestDTO {

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Payment type is required")
    private SimplePaymentType paymentType;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be positive")
    private BigDecimal amount;

    private String promotionCode;

    @Min(value = 1, message = "Subscription duration must be at least 1 month")
    private Integer subscriptionDurationMonths;

    // For Stripe integration
    private String stripePaymentIntentId;
    private String stripeSessionId;
}

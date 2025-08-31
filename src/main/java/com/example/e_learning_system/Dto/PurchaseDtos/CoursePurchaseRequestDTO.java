package com.example.e_learning_system.Dto.PurchaseDtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoursePurchaseRequestDTO {

    @NotNull(message = "User ID is required")
    private Integer userId;

    private String promotionCode;

    // For Stripe integration
    private String stripePaymentIntentId;
    private String stripeSessionId;
}

package com.example.e_learning_system.Dto.PurchaseDtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequestDTO {

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Subscription duration is required")
    @Pattern(regexp = "^(1|3|6)$", message = "Subscription duration must be 1, 3, or 6 months")
    private String subscriptionDurationMonths;

    private String promotionCode;

    // For Stripe integration
    private String stripePaymentIntentId;
    private String stripeSessionId;

    // Convenience method to get duration as integer
    public Integer getSubscriptionDurationAsInt() {
        return Integer.valueOf(subscriptionDurationMonths);
    }
}

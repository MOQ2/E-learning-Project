package com.example.e_learning_system.Dto.PaymentDtos;

import com.example.e_learning_system.Config.SimplePaymentType;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String cardNumber;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "\\d{3}", message = "CVV must be 3 digits")
    private String cvv;

    @NotBlank(message = "Expiration date is required")
    @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Expiration date must be in MM/YY format")
    private String expirationDate;

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;
}

package com.example.e_learning_system.Dto.PromotionDtos;

import jakarta.validation.constraints.*;
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
public class PromotionCodeRequestDTO {
    
    @NotBlank(message = "Promotion code is required")
    @Size(max = 50, message = "Code cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code can only contain uppercase letters, numbers, underscores and hyphens")
    private String code;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0.0", message = "Discount percentage must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100")
    private BigDecimal discountPercentage;
    
    @DecimalMin(value = "0.0", message = "Discount amount must be 0 or greater")
    private BigDecimal discountAmount;
    
    @Min(value = 1, message = "Max uses must be at least 1")
    private Integer maxUses;
    
    private LocalDateTime validFrom;
    
    @Future(message = "Valid until date must be in the future")
    private LocalDateTime validUntil;
    
    @Builder.Default
    private Boolean applicableToCourses = true;
    
    @Builder.Default
    private Boolean applicableToPackages = true;
    
    @Builder.Default
    private Boolean isActive = true;
}

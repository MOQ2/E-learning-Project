package com.example.e_learning_system.Dto.PromotionDtos;

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
public class PromotionCodeResponseDTO {
    private Integer id;
    private String code;
    private String description;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private Integer maxUses;
    private Integer currentUses;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean applicableToCourses;
    private Boolean applicableToPackages;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

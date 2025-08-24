package com.example.e_learning_system.Mapper;

import com.example.e_learning_system.Dto.PromotionCodeRequestDTO;
import com.example.e_learning_system.Dto.PromotionCodeResponseDTO;
import com.example.e_learning_system.Entities.PromotionCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PromotionCodeMapper {

    public PromotionCode requestDtoToEntity(PromotionCodeRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return PromotionCode.builder()
                .code(dto.getCode().toUpperCase())
                .description(dto.getDescription())
                .discountPercentage(dto.getDiscountPercentage())
                .discountAmount(dto.getDiscountAmount())
                .maxUses(dto.getMaxUses())
                .validFrom(dto.getValidFrom())
                .validUntil(dto.getValidUntil())
                .applicableToCourses(dto.getApplicableToCourses())
                .applicableToPackages(dto.getApplicableToPackages())
                .isActive(dto.getIsActive())
                .currentUses(0)
                .build();
    }

    public PromotionCodeResponseDTO entityToResponseDto(PromotionCode entity) {
        if (entity == null) {
            return null;
        }

        return PromotionCodeResponseDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .discountPercentage(entity.getDiscountPercentage())
                .discountAmount(entity.getDiscountAmount())
                .maxUses(entity.getMaxUses())
                .currentUses(entity.getCurrentUses())
                .validFrom(entity.getValidFrom())
                .validUntil(entity.getValidUntil())
                .applicableToCourses(entity.getApplicableToCourses())
                .applicableToPackages(entity.getApplicableToPackages())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntityFromRequestDto(PromotionCode entity, PromotionCodeRequestDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setCode(dto.getCode().toUpperCase());
        entity.setDescription(dto.getDescription());
        entity.setDiscountPercentage(dto.getDiscountPercentage());
        entity.setDiscountAmount(dto.getDiscountAmount());
        entity.setMaxUses(dto.getMaxUses());
        entity.setValidFrom(dto.getValidFrom());
        entity.setValidUntil(dto.getValidUntil());
        entity.setApplicableToCourses(dto.getApplicableToCourses());
        entity.setApplicableToPackages(dto.getApplicableToPackages());
        entity.setIsActive(dto.getIsActive());
        entity.setUpdatedAt(LocalDateTime.now());
    }
}

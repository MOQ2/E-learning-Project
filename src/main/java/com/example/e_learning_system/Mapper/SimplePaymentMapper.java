package com.example.e_learning_system.Mapper;

import com.example.e_learning_system.Config.SimplePaymentStatus;
import com.example.e_learning_system.Dto.SimplePaymentRequestDTO;
import com.example.e_learning_system.Dto.SimplePaymentResponseDTO;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.Package;
import com.example.e_learning_system.Entities.PromotionCode;
import com.example.e_learning_system.Entities.SimplePayment;
import com.example.e_learning_system.Entities.UserEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class SimplePaymentMapper {

    public SimplePayment requestDtoToEntity(SimplePaymentRequestDTO dto, UserEntity user, Course course, Package packageEntity, PromotionCode promotionCode) {
        if (dto == null) {
            return null;
        }

        return SimplePayment.builder()
                .user(user)
                .course(course)
                .packageEntity(packageEntity)
                .paymentType(dto.getPaymentType())
                .amount(dto.getAmount())
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(dto.getAmount())
                .promotionCode(promotionCode)
                .status(SimplePaymentStatus.PENDING)
                .subscriptionDurationMonths(dto.getSubscriptionDurationMonths())
                .build();
    }

    public SimplePaymentResponseDTO entityToResponseDto(SimplePayment entity) {
        if (entity == null) {
            return null;
        }

        return SimplePaymentResponseDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userEmail(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .courseName(entity.getCourse() != null ? entity.getCourse().getName() : null)
                .packageId(entity.getPackageEntity() != null ? entity.getPackageEntity().getId() : null)
                .packageName(entity.getPackageEntity() != null ? entity.getPackageEntity().getName() : null)
                .paymentType(entity.getPaymentType())
                .amount(entity.getAmount())
                .discountAmount(entity.getDiscountAmount())
                .finalAmount(entity.getFinalAmount())
                .promotionCode(entity.getPromotionCode() != null ? entity.getPromotionCode().getCode() : null)
                .status(entity.getStatus())
                .paymentDate(entity.getPaymentDate())
                .subscriptionDurationMonths(entity.getSubscriptionDurationMonths())
                .stripePaymentIntentId(entity.getStripePaymentIntentId())
                .stripeSessionId(entity.getStripeSessionId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updatePaymentStatus(SimplePayment entity, SimplePaymentStatus status, String stripePaymentIntentId) {
        if (entity == null) {
            return;
        }

        entity.setStatus(status);
        entity.setStripePaymentIntentId(stripePaymentIntentId);
        if (status == SimplePaymentStatus.COMPLETED) {
            entity.setPaymentDate(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
    }

    public void applyDiscount(SimplePayment entity, BigDecimal discountAmount) {
        if (entity == null || discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        entity.setDiscountAmount(discountAmount);
        entity.setFinalAmount(entity.getAmount().subtract(discountAmount));
    }
}

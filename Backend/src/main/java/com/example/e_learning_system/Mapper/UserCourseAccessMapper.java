package com.example.e_learning_system.Mapper;

import com.example.e_learning_system.Config.AccessType;
import com.example.e_learning_system.Dto.UserCourseAccessResponseDTO;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.Package;
import com.example.e_learning_system.Entities.SimplePayment;
import com.example.e_learning_system.Entities.UserCourseAccess;
import com.example.e_learning_system.Entities.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserCourseAccessMapper {

    public UserCourseAccess createAccess(UserEntity user, Course course, Package packageEntity, AccessType accessType, LocalDateTime accessUntil, SimplePayment payment) {
        return UserCourseAccess.builder()
                .user(user)
                .course(course)
                .packageEntity(packageEntity)
                .accessType(accessType)
                .accessUntil(accessUntil)
                .isActive(true)
                .payment(payment)
                .build();
    }

    public UserCourseAccessResponseDTO entityToResponseDto(UserCourseAccess entity) {
        if (entity == null) {
            return null;
        }

        return UserCourseAccessResponseDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userEmail(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .courseName(entity.getCourse() != null ? entity.getCourse().getName() : null)
                .packageId(entity.getPackageEntity() != null ? entity.getPackageEntity().getId() : null)
                .packageName(entity.getPackageEntity() != null ? entity.getPackageEntity().getName() : null)
                .accessType(entity.getAccessType())
                .accessUntil(entity.getAccessUntil())
                .isActive(entity.getIsActive())
                .paymentId(entity.getPayment() != null ? entity.getPayment().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void deactivateAccess(UserCourseAccess entity) {
        if (entity == null) {
            return;
        }

        entity.setIsActive(false);
        entity.setUpdatedAt(LocalDateTime.now());
    }

    public void extendAccess(UserCourseAccess entity, LocalDateTime newAccessUntil) {
        if (entity == null) {
            return;
        }

        entity.setAccessUntil(newAccessUntil);
        entity.setUpdatedAt(LocalDateTime.now());
    }
}

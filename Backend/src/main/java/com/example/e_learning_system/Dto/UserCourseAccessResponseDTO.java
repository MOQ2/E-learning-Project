package com.example.e_learning_system.Dto;

import com.example.e_learning_system.Config.AccessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCourseAccessResponseDTO {
    private Integer id;
    private Integer userId;
    private String userEmail;
    private Integer courseId;
    private String courseName;
    private Integer packageId;
    private String packageName;
    private AccessType accessType;
    private LocalDateTime accessUntil;
    private Boolean isActive;
    private Integer paymentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

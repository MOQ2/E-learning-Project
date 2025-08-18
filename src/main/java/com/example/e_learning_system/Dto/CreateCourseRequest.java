package com.example.e_learning_system.Dto;

import com.example.e_learning_system.Config.AccessModel;
import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.DifficultyLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {
    @NotBlank(message = "Course name is required")
    @Size(max = 255, message = "Course name must not exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be positive")
    private BigDecimal oneTimePrice;

    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currency;

    private String thumbnail;
    private String previewVideoUrl;

    @Min(value = 1, message = "Estimated duration must be at least 1 hour")
    private Integer estimatedDurationInHours;

    private Boolean isActive = true;
    private Boolean isFree = false;
    private CourseStatus status = CourseStatus.DRAFT;
    private AccessModel accessModel;
    private DifficultyLevel difficultyLevel;
}
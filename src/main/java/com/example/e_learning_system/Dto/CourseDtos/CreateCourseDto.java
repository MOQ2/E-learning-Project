package com.example.e_learning_system.Dto.CourseDtos;

import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.Currency;
import com.example.e_learning_system.Config.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CreateCourseDto {
    @NotBlank(message = "Course name is required")
    @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "One-time price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal oneTimePrice;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotBlank(message = "Thumbnail URL is required")
    private String thumbnail;

    @NotBlank(message = "Preview video URL is required")
    private String previewVideoUrl;

    @Positive(message = "Estimated duration must be positive")
    private int estimatedDurationInHours;

    @NotNull(message = "Course status is required")
    private CourseStatus status;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel;

    private boolean isActive;
}

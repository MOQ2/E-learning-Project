package com.example.e_learning_system.Dto.CourseDtos;

import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.Currency;
import com.example.e_learning_system.Config.DifficultyLevel;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import com.example.e_learning_system.Config.Category;

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
    @DecimalMin(value = "0.0", inclusive = true, message = "Course price cannot be negative")
    private BigDecimal oneTimePrice;

    private BigDecimal subscriptionPriceMonthly;
    private BigDecimal subscriptionPrice3Months;
    private BigDecimal subscriptionPrice6Months;
    private Boolean allowsSubscription;

    private Currency currency;

    private Category category;

    @NotNull(message = "Thumbnail is required")
    private Integer thumbnail;

    @NotNull(message = "Estimated duration is required")
    @Min(value = 1, message = "Estimated duration must be at least 1 hour")
    private int estimatedDurationInHours;

    @NotNull(message = "Course status is required")
    private CourseStatus status;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel;
    @NotNull(message = "Tags are required")
    private List<String> tags;


    private boolean isActive;
}

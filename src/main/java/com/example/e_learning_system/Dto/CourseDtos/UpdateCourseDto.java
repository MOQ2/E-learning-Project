package com.example.e_learning_system.Dto.CourseDtos;


import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.Currency;
import com.example.e_learning_system.Config.DifficultyLevel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UpdateCourseDto{

    @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Positive(message = "Price must be greater than zero")
    private BigDecimal oneTimePrice;

    private Currency currency;

    private String thumbnail;

    private String previewVideoUrl;

    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDurationInHours; // use Integer to allow null

    private CourseStatus status;

    private DifficultyLevel difficultyLevel;

    private Boolean isActive;

}

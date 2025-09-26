package com.example.e_learning_system.Dto.CourseDtos;


import com.example.e_learning_system.Config.Category;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UpdateCourseDto{

    @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal oneTimePrice;

    private BigDecimal subscriptionPriceMonthly;
    private BigDecimal subscriptionPrice3Months;
    private BigDecimal subscriptionPrice6Months;
    private Boolean allowsSubscription;

    private Currency currency;

    private Integer thumbnail;

    @Min(value = 1, message = "Estimated duration must be positive")
    private Integer estimatedDurationInHours; // use Integer to allow null

    private CourseStatus status;

    private DifficultyLevel difficultyLevel;

    private Boolean isActive;
    
    private Category category;
    private List<String> tags;

}

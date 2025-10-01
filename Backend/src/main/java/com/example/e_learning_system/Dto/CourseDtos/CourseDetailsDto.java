package com.example.e_learning_system.Dto.CourseDtos;

import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.Currency;
import com.example.e_learning_system.Config.DifficultyLevel;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import com.example.e_learning_system.Config.Category;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailsDto {
    private int id;
    private String name;
    private String description;
    private BigDecimal oneTimePrice;
    private BigDecimal subscriptionPriceMonthly;
    private BigDecimal subscriptionPrice3Months;
    private BigDecimal subscriptionPrice6Months;
    private Boolean allowsSubscription;
    private Currency currency;
    private Category category;
    private Integer thumbnail;
    private int estimatedDurationInHours;
    private CourseStatus status;
    private DifficultyLevel difficultyLevel;
    private boolean isActive;
    private List<CourseModuleDto> modules;
    private Set<TagDto> tags;
    private String instructor;
    // runtime statistics
    private Integer enrolledCount;
    private Double averageRating;
    private Integer reviewCount;
    private String thumbnailUrl;
}
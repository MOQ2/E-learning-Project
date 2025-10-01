package com.example.e_learning_system.Dto.CourseDtos;

import com.example.e_learning_system.Config.Category;
import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.Currency;
import com.example.e_learning_system.Config.DifficultyLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummaryDto {
    private int id ;
    private String name ;
    private String description;
    private CourseStatus status;
    private DifficultyLevel difficultyLevel;
    private boolean isActive ;
    private BigDecimal oneTimePrice ;
    private Currency currency;
    private Set<TagDto> tags;
    private Integer thumbnail;
    private String thumbnailUrl;
    private String instructor;
    private int estimatedDurationInHours;
    private Category category;
    private Double averageRating;
    private Integer reviewCount;
    private Integer enrolledCount;
}

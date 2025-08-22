package com.example.e_learning_system.Dto.CourseDtos;

import com.example.e_learning_system.Config.AccessModel;
import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.Currency;
import com.example.e_learning_system.Config.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailsDto {
    private int id;
    private String name;
    private String description;
    private BigDecimal oneTimePrice;
    private Currency currency;
    private String thumbnail;
    private String previewVideoUrl;
    private int estimatedDurationInHours;
    private CourseStatus status;
    private DifficultyLevel difficultyLevel;
    private boolean isActive;

    private List<CourseModuleDto> modules;
}
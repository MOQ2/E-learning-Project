package com.example.e_learning_system.Dto;

import com.example.e_learning_system.Config.AccessModel;
import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.Currency;
import com.example.e_learning_system.Config.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal oneTimePrice;
    private Currency currency;
    private String thumbnail;
    private String previewVideoUrl;
    private int estimatedDurationInHours;
    private boolean isActive;
    private boolean isFree;
    private CourseStatus status;
    private AccessModel accessModel;
    private DifficultyLevel difficultyLevel;
    private Integer createdById;
    private String createdByName;
    private List<ModuleDto> modules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
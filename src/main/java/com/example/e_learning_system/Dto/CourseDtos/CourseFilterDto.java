package com.example.e_learning_system.Dto.CourseDtos;



import com.example.e_learning_system.Config.AccessModel;
import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.Currency;
import com.example.e_learning_system.Config.DifficultyLevel;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseFilterDto {

    // Text search
    private String name;
    private String description;

    // Price range
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Currency filter
    private Currency currency;



    // Duration filter
    private Integer minDurationHours;
    private Integer maxDurationHours;

    // Flags
    private Boolean isActive;
    private Boolean isFree;

    // Relations (IDs)
    private Integer createdByUserId;    // filter by creator


    private List<CourseStatus> statuses;
    private List<DifficultyLevel> difficultyLevels;


}

package com.example.e_learning_system.Dto.CourseDtos;

import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.Currency;
import com.example.e_learning_system.Config.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;



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
}

package com.example.e_learning_system.Dto.CourseDtos;

import com.example.e_learning_system.Dto.ModuleDtos.ModuleSummaryDto;
import lombok.Data;


@Data
public class CourseModuleDto {
    private int moduleOrder;
    private ModuleSummaryDto module;
}

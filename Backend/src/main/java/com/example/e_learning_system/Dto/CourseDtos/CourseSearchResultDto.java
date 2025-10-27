package com.example.e_learning_system.Dto.CourseDtos;

import com.example.e_learning_system.Config.Category;
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
public class CourseSearchResultDto {
    private int id;
    private String name;
    private String description;
    private String thumbnailUrl;
    private String instructor;
    private int estimatedDurationInHours;
    private Integer lessonCount;
    private BigDecimal oneTimePrice;
    private String currency;
    private Category category;
    private Set<TagDto> tags;
    private String matchType; // "title", "description", "tag", "category"
}

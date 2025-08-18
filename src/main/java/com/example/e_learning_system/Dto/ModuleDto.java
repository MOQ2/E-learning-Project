package com.example.e_learning_system.Dto;

import com.example.e_learning_system.Config.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDto {
    private Integer id;
    private String name;
    private String description;
    private boolean isActive;
    private int estimatedDuration;
    private CourseStatus courseStatus;
    private Integer createdById;
    private String createdByName;
    private List<VideoDto> videos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

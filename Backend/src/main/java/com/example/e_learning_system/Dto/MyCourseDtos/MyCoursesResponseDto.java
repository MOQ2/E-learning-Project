package com.example.e_learning_system.Dto.MyCourseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for the complete My Courses dashboard response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCoursesResponseDto {
    private TeacherStatsDto stats;
    private List<TeacherCourseDto> courses;
    private List<TeacherCourseDto> recentlyUpdatedCourses;
    private List<TeacherCourseDto> topPerformingCourses;
}

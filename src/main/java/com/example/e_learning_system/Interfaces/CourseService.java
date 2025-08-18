package com.example.e_learning_system.Interfaces;

import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.DifficultyLevel;
import com.example.e_learning_system.Dto.CourseDto;
import com.example.e_learning_system.Dto.CreateCourseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CourseService {
    CourseDto createCourse(CreateCourseRequest request, Integer createdById);
    CourseDto getCourseById(Integer id);
    List<CourseDto> getCoursesByUser(Integer userId);
    List<CourseDto> getActiveCourses(Pageable pageable);
    List<CourseDto> getPublishedCourses(Pageable pageable);
    List<CourseDto> getCoursesByStatus(CourseStatus status);
    List<CourseDto> getCoursesByDifficulty(DifficultyLevel difficulty);
    List<CourseDto> getFreeCourses();
    List<CourseDto> getCoursesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    CourseDto updateCourse(Integer id, CreateCourseRequest request);
    CourseDto publishCourse(Integer id);
    void deleteCourse(Integer id);
    void deactivateCourse(Integer id);
    List<CourseDto> searchCoursesByName(String name);
}
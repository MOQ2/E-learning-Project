package com.example.e_learning_system.Interfaces;

import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.DifficultyLevel;
import com.example.e_learning_system.Dto.CourseDtos.*;
import com.example.e_learning_system.Dto.CreateCourseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CourseService {

    List<CourseSummaryDto>  getCourses();
    Page<CourseSummaryDto> getCourses(CourseFilterDto filterDto, Pageable pageable);
    CourseDetailsDto createCourse(CreateCourseDto request, Integer createdById );
    CourseDetailsDto getCourseById(Integer id);
    public void updateCourse(UpdateCourseDto updateCourseDto , int courseId);
    void deleteCourse(Integer id);
    void deactivateCourse(Integer id , boolean deactivate);
    void addMoudelToCourse (int courseId , int moudelId , int order);
    void removeMoudelFromCourse (int courseId , int moudelId);

}
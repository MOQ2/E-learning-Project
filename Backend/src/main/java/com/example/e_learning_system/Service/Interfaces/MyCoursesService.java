package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Dto.MyCourseDtos.MyCoursesResponseDto;
import com.example.e_learning_system.Dto.MyCourseDtos.TeacherCourseDto;
import com.example.e_learning_system.Dto.MyCourseDtos.TeacherStatsDto;

import java.util.List;

/**
 * Service interface for teacher's course management dashboard (My Courses)
 */
public interface MyCoursesService {
    
    /**
     * Get complete dashboard for a teacher
     * @param teacherId The teacher's user ID
     * @return Dashboard with stats and courses
     */
    MyCoursesResponseDto getMyCoursesDashboard(Integer teacherId);
    
    /**
     * Get all courses created by a teacher
     * @param teacherId The teacher's user ID
     * @return List of courses with statistics
     */
    List<TeacherCourseDto> getTeacherCourses(Integer teacherId);
    
    /**
     * Get teacher statistics
     * @param teacherId The teacher's user ID
     * @return Statistics DTO
     */
    TeacherStatsDto getTeacherStats(Integer teacherId);
    
    /**
     * Get recently updated courses
     * @param teacherId The teacher's user ID
     * @param limit Maximum number of courses to return
     * @return List of recently updated courses
     */
    List<TeacherCourseDto> getRecentlyUpdatedCourses(Integer teacherId, int limit);
    
    /**
     * Get top performing courses by enrollment
     * @param teacherId The teacher's user ID
     * @param limit Maximum number of courses to return
     * @return List of top performing courses
     */
    List<TeacherCourseDto> getTopPerformingCourses(Integer teacherId, int limit);
    
    /**
     * Get course details with statistics
     * @param teacherId The teacher's user ID
     * @param courseId The course ID
     * @return Course details with statistics
     */
    TeacherCourseDto getCourseDetails(Integer teacherId, Integer courseId);
}

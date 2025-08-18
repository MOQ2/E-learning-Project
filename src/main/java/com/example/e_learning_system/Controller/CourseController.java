package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.DifficultyLevel;
import com.example.e_learning_system.Dto.CourseDto;
import com.example.e_learning_system.Dto.CreateCourseRequest;
import com.example.e_learning_system.Dto.ApiResponse;
import com.example.e_learning_system.Interfaces.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;



    //!! to-do extract the user id from jwt token
    @PostMapping
    public ResponseEntity<ApiResponse<CourseDto>> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @RequestParam Integer createdById) {

        CourseDto course = courseService.createCourse(request, createdById);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully", course));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDto>> getCourseById(@PathVariable Integer id) {
        CourseDto course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getCoursesByUser(@PathVariable Integer userId) {
        List<CourseDto> courses = courseService.getCoursesByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }
    //! use pagable
    @GetMapping("/published")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getPublishedCourses(Pageable pageable) {
        List<CourseDto> courses = courseService.getPublishedCourses(pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/free")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getFreeCourses() {
        List<CourseDto> courses = courseService.getFreeCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getCoursesByDifficulty(@PathVariable DifficultyLevel difficulty) {
        List<CourseDto> courses = courseService.getCoursesByDifficulty(difficulty);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CourseDto>>> searchCourses(@RequestParam String name) {
        List<CourseDto> courses = courseService.searchCoursesByName(name);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }
    //! the id need to be exctracted from the jwt token
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDto>> updateCourse(
            @PathVariable Integer id,
            @Valid @RequestBody CreateCourseRequest request) {

        CourseDto course = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", course));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<CourseDto>> publishCourse(@PathVariable Integer id) {
        CourseDto course = courseService.publishCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course published successfully", course));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateCourse(@PathVariable Integer id) {
        courseService.deactivateCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deactivated successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Integer id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully", null));
    }
}

package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Config.Category;
import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.Currency;
import com.example.e_learning_system.Config.DifficultyLevel;
import com.example.e_learning_system.Dto.ApiResponse;
import com.example.e_learning_system.Dto.CourseDtos.CourseDetailsDto;
import com.example.e_learning_system.Dto.CourseDtos.CourseFilterDto;
import com.example.e_learning_system.Dto.CourseDtos.CourseSummaryDto;
import com.example.e_learning_system.Dto.CourseDtos.CreateCourseDto;
import com.example.e_learning_system.Dto.CourseDtos.TagDto;
import com.example.e_learning_system.Dto.CourseDtos.UpdateCourseDto;
import com.example.e_learning_system.Service.Interfaces.CourseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;


    /**
     * Get all courses without pagination
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CourseSummaryDto>>> getAllCourses() {
        List<CourseSummaryDto> courses = courseService.getCourses();
        return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
    }

    /**
     * Get courses with filtering and pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourseSummaryDto>>> getCourses(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Currency currency,
            @RequestParam(required = false) Integer minDurationHours,
            @RequestParam(required = false) Integer maxDurationHours,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isFree,
            @RequestParam(required = false) Integer createdByUserId,
            @RequestParam(required = false) List<CourseStatus> statuses,
            @RequestParam(required = false) List<DifficultyLevel> difficultyLevels,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) List<Category> categories
            ) {

                
            List<TagDto> tagList = new ArrayList<>();

            if (tags != null && !tags.isEmpty()) {
            try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    tagList = objectMapper.readValue(tags, new TypeReference<List<TagDto>>() {});
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Invalid tags format", e);
                }
            }

        // Convert List<TagDto> to List<String> (tag names)
        List<String> tagNames = new ArrayList<>();
        for (TagDto tag : tagList) {
            tagNames.add(tag.getName());
        }

        CourseFilterDto filterDto = CourseFilterDto.builder()
                .name(name)
                .description(description)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .currency(currency)
                .minDurationHours(minDurationHours)
                .maxDurationHours(maxDurationHours)
                .isActive(isActive)
                .isFree(isFree)
                .createdByUserId(createdByUserId)
                .statuses(statuses)
                .difficultyLevels(difficultyLevels)
                .categories(categories)
                .tags(tagNames)
                .build();

        Page<CourseSummaryDto> courses = courseService.getCourses(filterDto, pageable);
        return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
    }

    /**
     * Get course details by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDetailsDto>> getCourseById(@PathVariable Integer id) {
        CourseDetailsDto course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success("Course retrieved successfully", course));
    }

    /**
     * Create a new course
     * TODO: Extract user ID from JWT token instead of using request parameter
     */
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CourseDetailsDto>> createCourse(
            @Valid @ModelAttribute CreateCourseDto createCourseDto
            ) {
        //TODO use this instead of 1 in the create course method 
        //int user_id = Math.toIntExact(UserUtil.getCurrentUserId());
        
        // TODO - replace with actual user ID from JWT
        log.info("tags list is {}" , createCourseDto.getTags());
        CourseDetailsDto course = courseService.createCourse(createCourseDto, 1);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully", course));
    }

    /**
     * Update an existing course
     */
    @PutMapping(value = "/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CourseDetailsDto>> updateCourse(
            @PathVariable Integer id,
            @Valid @ModelAttribute UpdateCourseDto updateCourseDto) {
                
        courseService.updateCourse(updateCourseDto , id);
        CourseDetailsDto updatedCourse = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", updatedCourse));
    }

    /**
     * Delete a course
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Integer id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully", null));
    }

    /**
     * Deactivate a course
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateCourse(@PathVariable Integer id) {
        courseService.deactivateCourse(id, true);
        return ResponseEntity.ok(ApiResponse.success("Course deactivated successfully", null));
    }

    // ========== CONVENIENCE ENDPOINTS ==========
    // These endpoints provide convenient access to commonly used filters

    /**
     * Get active courses only
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Page<CourseSummaryDto>>> getActiveCourses(
            @PageableDefault(size = 20) Pageable pageable) {

        CourseFilterDto filterDto = CourseFilterDto.builder()
                .isActive(true)
                .build();

        Page<CourseSummaryDto> courses = courseService.getCourses(filterDto, pageable);
        return ResponseEntity.ok(ApiResponse.success("Active courses retrieved successfully", courses));
    }

    /**
     * Get published courses only
     */
    @GetMapping("/published")
    public ResponseEntity<ApiResponse<Page<CourseSummaryDto>>> getPublishedCourses(
            @PageableDefault(size = 20) Pageable pageable) {

        CourseFilterDto filterDto = CourseFilterDto.builder()
                .isActive(true)
                .statuses(List.of(CourseStatus.PUBLISHED))
                .build();

        Page<CourseSummaryDto> courses = courseService.getCourses(filterDto, pageable);
        return ResponseEntity.ok(ApiResponse.success("Published courses retrieved successfully", courses));
    }

    /**
     * Get free courses only
     */
    @GetMapping("/free")
    public ResponseEntity<ApiResponse<Page<CourseSummaryDto>>> getFreeCourses(
            @PageableDefault(size = 20) Pageable pageable) {

        CourseFilterDto filterDto = CourseFilterDto.builder()
                .isActive(true)
                .isFree(true)
                .build();

        Page<CourseSummaryDto> courses = courseService.getCourses(filterDto, pageable);
        return ResponseEntity.ok(ApiResponse.success("Free courses retrieved successfully", courses));
    }

    /**
     * Get courses by difficulty level
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<ApiResponse<Page<CourseSummaryDto>>> getCoursesByDifficulty(
            @PathVariable DifficultyLevel difficulty,
            @PageableDefault(size = 20) Pageable pageable) {

        CourseFilterDto filterDto = CourseFilterDto.builder()
                .isActive(true)
                .difficultyLevels(List.of(difficulty))
                .build();

        Page<CourseSummaryDto> courses = courseService.getCourses(filterDto, pageable);
        return ResponseEntity.ok(ApiResponse.success("Courses by difficulty retrieved successfully", courses));
    }

    /**
     * Get courses by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<CourseSummaryDto>>> getCoursesByStatus(
            @PathVariable CourseStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        CourseFilterDto filterDto = CourseFilterDto.builder()
                .statuses(List.of(status))
                .build();

        Page<CourseSummaryDto> courses = courseService.getCourses(filterDto, pageable);
        return ResponseEntity.ok(ApiResponse.success("Courses by status retrieved successfully", courses));
    }

    /**
     * Get courses by creator/user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<CourseSummaryDto>>> getCoursesByUser(
            @PathVariable Integer userId,
            @PageableDefault(size = 20) Pageable pageable) {

        CourseFilterDto filterDto = CourseFilterDto.builder()
                .createdByUserId(userId)
                .build();

        Page<CourseSummaryDto> courses = courseService.getCourses(filterDto, pageable);
        return ResponseEntity.ok(ApiResponse.success("User courses retrieved successfully", courses));
    }

    /**
     * Search courses by name
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<CourseSummaryDto>>> searchCourses(
            @RequestParam String name,
            @RequestParam(required = false) Boolean activeOnly,
            @PageableDefault(size = 20) Pageable pageable) {

        CourseFilterDto.CourseFilterDtoBuilder filterBuilder = CourseFilterDto.builder()
                .name(name);

        // If activeOnly is specified and true, filter by active courses
        if (activeOnly != null && activeOnly) {
            filterBuilder.isActive(true);
        }

        CourseFilterDto filterDto = filterBuilder.build();

        Page<CourseSummaryDto> courses = courseService.getCourses(filterDto, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", courses));
    }

    /**
     * Get courses within price range
     */
    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<Page<CourseSummaryDto>>> getCoursesByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable) {

        if (minPrice.compareTo(maxPrice) > 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Minimum price cannot be greater than maximum price"));
        }

        CourseFilterDto filterDto = CourseFilterDto.builder()
                .isActive(true)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();

        Page<CourseSummaryDto> courses = courseService.getCourses(filterDto, pageable);
        return ResponseEntity.ok(ApiResponse.success("Courses in price range retrieved successfully", courses));
    }

    /**
     * create and add a module to a course
     **/
    @PostMapping ("{courseId}/modules/{moduleId}/{moduleOrder}")
        public ResponseEntity<ApiResponse<Void>> getCoursesByModule(
                @PathVariable int courseId ,
                @PathVariable int moduleId,
                @PathVariable int moduleOrder
        ){

            courseService.addModuleToCourse(courseId, moduleId, moduleOrder);
            return ResponseEntity.ok(ApiResponse.success("module %d have been added to course %d in order {}".formatted(moduleId,courseId,moduleOrder),null));
        }

    @DeleteMapping("{courseId}/modules/{moduleId}/")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @PathVariable int courseId,
            @PathVariable int moduleId
    ){
        courseService.removeModuleFromCourse(courseId, moduleId);
        return ResponseEntity.ok(ApiResponse.success("module %d have been removed from course %d".formatted(moduleId,courseId),null));
    }

    @PutMapping("{courseId}/modules/{moduleId}/order/{newOrder}")
    public ResponseEntity<ApiResponse<Void>> updateModuleOrderInCourse(
            @PathVariable int courseId,
            @PathVariable int moduleId,
            @PathVariable int newOrder
    ){
        courseService.updateModuleOrderInCourse(courseId, moduleId, newOrder);
        return ResponseEntity.ok(ApiResponse.success("Module %d order updated to %d in course %d".formatted(moduleId, newOrder, courseId), null));
    }

    @PutMapping("{courseId}/modules/order")
    public ResponseEntity<ApiResponse<Void>> updateModuleOrdersInCourse(
            @PathVariable int courseId,
            @RequestBody java.util.List<com.example.e_learning_system.Dto.OrderDtos.IdOrderDto> orders
    ){
        courseService.updateModuleOrdersInCourse(courseId, orders);
        return ResponseEntity.ok(ApiResponse.success("Module orders updated successfully", null));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<TagDto>>> getCategories() {

        return ResponseEntity.ok(ApiResponse.success("Tags retrieved successfully", courseService.getAllTags()));
    }




}
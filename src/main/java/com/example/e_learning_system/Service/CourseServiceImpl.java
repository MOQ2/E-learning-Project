package com.example.e_learning_system.Service;






import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.DifficultyLevel;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.Module;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Dto.CourseDto;
import com.example.e_learning_system.Dto.ModuleDto;
import com.example.e_learning_system.Dto.CreateCourseRequest;
import com.example.e_learning_system.Repository.CourseRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.Interfaces.CourseService;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public CourseDto createCourse(CreateCourseRequest request, Integer createdById) {
        log.info("Creating new course: {} for user: {}", request.getName(), createdById);

        UserEntity creator = userRepository.findById(createdById)
                .orElseThrow(() -> ResourceNotFound.userNotFound(createdById+""));

        // Validate business rules
        validateCourseRequest(request);

        Course course = Course.builder()
                .name(request.getName())
                .description(request.getDescription())
                .oneTimePrice(request.getOneTimePrice())
                .currency(request.getCurrency())
                .thumbnail(request.getThumbnail())
                .previewVideoUrl(request.getPreviewVideoUrl())
                .estimatedDrationInHours(request.getEstimatedDurationInHours())
                .isActive(request.getIsActive())
                .status(request.getStatus())
                .accessModel(request.getAccessModel())
                .difficultyLevel(request.getDifficultyLevel())
                .createdBy(creator)
                .build();

        Course savedCourse = courseRepository.save(course);

        log.info("Course created successfully with id: {}", savedCourse.getId());
        return mapToDto(savedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourseById(Integer id) {
        log.debug("Fetching course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(id+""));

        return mapToDto(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByUser(Integer userId) {
        log.debug("Fetching courses for user: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFound.userNotFound(userId+""));

        return courseRepository.findByCreatedBy(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getActiveCourses(Pageable pageable) {
        log.debug("Fetching active courses with pagination");

        return courseRepository.findByIsActive(true).stream().map(this::mapToDto).collect(Collectors.toList());

    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getPublishedCourses(Pageable pageable) {
        log.debug("Fetching published courses with pagination");

        return courseRepository.findByIsActiveAndStatus(true, CourseStatus.PUBLISHED).stream()
                .map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByStatus(CourseStatus status) {
        log.debug("Fetching courses with status: {}", status);

        return courseRepository.findByStatus(status)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByDifficulty(DifficultyLevel difficulty) {
        log.debug("Fetching courses with difficulty: {}", difficulty);

        return courseRepository.findByDifficultyLevel(difficulty)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getFreeCourses() {
        log.debug("Fetching free courses");

        return courseRepository.findByIsFreeAndIsActive(true, true)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Fetching courses with price range: {} - {}", minPrice, maxPrice);

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        return courseRepository.findByOneTimePriceBetween(minPrice, maxPrice)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CourseDto updateCourse(Integer id, CreateCourseRequest request) {
        log.info("Updating course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(id+""));

        // Validate business rules
        validateCourseRequest(request);


        if (course.getStatus() == CourseStatus.PUBLISHED && !canUpdatePublishedCourse(course)) {
            throw new RuntimeException("Cannot update published course with active enrollments");
        }

        // Update fields
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setOneTimePrice(request.getOneTimePrice());
        course.setCurrency(request.getCurrency());
        course.setThumbnail(request.getThumbnail());
        course.setPreviewVideoUrl(request.getPreviewVideoUrl());
        course.setEstimatedDrationInHours(request.getEstimatedDurationInHours());
        course.setActive(request.getIsActive());
        course.setStatus(request.getStatus());
        course.setAccessModel(request.getAccessModel());
        course.setDifficultyLevel(request.getDifficultyLevel());

        Course updatedCourse = courseRepository.save(course);

        log.info("Course updated successfully: {}", id);
        return mapToDto(updatedCourse);
    }

    @Override
    public CourseDto publishCourse(Integer id) {
        log.info("Publishing course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(id+""));

        // Validate course is ready for publishing
        validateCourseForPublishing(course);

        course.setStatus(CourseStatus.PUBLISHED);
        course.setActive(true);

        Course publishedCourse = courseRepository.save(course);

        log.info("Course published successfully: {}", id);
        return mapToDto(publishedCourse);
    }

    @Override
    public void deleteCourse(Integer id) {
        log.info("Deleting course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(id+""));

        // Check if course can be deleted (e.g., no active enrollments)
        if (!canDeleteCourse(course)) {
            throw new RuntimeException("Cannot delete course with active enrollments or dependencies");
        }

        courseRepository.deleteById(id);

        log.info("Course deleted successfully: {}", id);
    }

    @Override
    public void deactivateCourse(Integer id) {
        log.info("Deactivating course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(id+""));

        course.setActive(false);
        course.setStatus(CourseStatus.DRAFT); // Move back to draft when deactivated

        courseRepository.save(course);

        log.info("Course deactivated successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> searchCoursesByName(String name) {
        log.debug("Searching courses by name: {}", name);

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Search name cannot be empty");
        }

        return courseRepository.findByNameContainingIgnoreCase(name.trim())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Private helper methods

    private void validateCourseRequest(CreateCourseRequest request) {
        // Validate pricing logic
        if (!request.getIsFree() && (request.getOneTimePrice() == null || request.getOneTimePrice().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Paid courses must have a valid price greater than zero");
        }

        if (request.getIsFree() && request.getOneTimePrice() != null && request.getOneTimePrice().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("Free courses cannot have a price");
        }

        // Validate currency for paid courses
        if (!request.getIsFree() && (request.getCurrency() == null || request.getCurrency().trim().isEmpty())) {
            throw new IllegalArgumentException("Currency is required for paid courses");
        }

        // Validate estimated duration
        if (request.getEstimatedDurationInHours() != null && request.getEstimatedDurationInHours() <= 0) {
            throw new IllegalArgumentException("Estimated duration must be positive");
        }
    }

    private void validateCourseForPublishing(Course course) {
        List<String> validationErrors = new ArrayList<>();

        // Check required fields for publishing
        if (course.getName() == null || course.getName().trim().isEmpty()) {
            validationErrors.add("Course name is required for publishing");
        }

        if (course.getDescription() == null || course.getDescription().trim().isEmpty()) {
            validationErrors.add("Course description is required for publishing");
        }

        if (course.getDifficultyLevel() == null) {
            validationErrors.add("Difficulty level is required for publishing");
        }

        if (course.getAccessModel() == null) {
            validationErrors.add("Access model is required for publishing");
        }

        // Check if course has modules
        if (course.getCourseModules() == null || course.getCourseModules().isEmpty()) {
            validationErrors.add("Course must have at least one module to be published");
        }

        // Additional business rules for publishing
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            validationErrors.add("Cannot publish an archived course");
        }
        //! to-do  use the custome exception handelr for this
        if (!validationErrors.isEmpty()) {
            throw new RuntimeException("Course validation failed: " + String.join(", ", validationErrors));
        }
    }
    //! to-do
    private boolean canUpdatePublishedCourse(Course course) {

        return true; // Simplified for now
    }
    //! to-do
    private boolean canDeleteCourse(Course course) {

        return course.getStatus() == CourseStatus.DRAFT || course.getStatus() == CourseStatus.ARCHIVED;
    }

    private CourseDto mapToDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .oneTimePrice(course.getOneTimePrice())
                .currency(course.getCurrency())
                .thumbnail(course.getThumbnail())
                .previewVideoUrl(course.getPreviewVideoUrl())
                .estimatedDurationInHours(course.getEstimatedDrationInHours())
                .isActive(course.isActive())
                .isFree(course.isFree())
                .status(course.getStatus())
                .accessModel(course.getAccessModel())
                .difficultyLevel(course.getDifficultyLevel())
                .createdById(course.getCreatedBy().getId())
                .createdByName(course.getCreatedBy().getName())
                .modules(mapModulesToDto(course.getModules()))
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private List<ModuleDto> mapModulesToDto(Set<Module> modules) {
        if (modules == null || modules.isEmpty()) {
            return new ArrayList<>();
        }

        return modules.stream()
                .map(this::mapModuleToDto)
                .collect(Collectors.toList());
    }

    private ModuleDto mapModuleToDto(Module module) {
        return ModuleDto.builder()
                .id(module.getId())
                .name(module.getName())
                .description(module.getDescription())
                .isActive(module.isActive())
                .estimatedDuration(module.getEstimatedDuration())
                .courseStatus(module.getCourseStatus())
                .createdById(module.getCreatedBy().getId())
                .createdByName(module.getCreatedBy().getName())
                .videos(new ArrayList<>()) // Avoid deep nesting, load separately if needed
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .build();
    }
}

package com.example.e_learning_system.Service;

import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Config.DifficultyLevel;
import com.example.e_learning_system.Dto.CourseDtos.*;
import com.example.e_learning_system.Dto.CreateCourseRequest;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.CourseModules;
import com.example.e_learning_system.Entities.Module;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Interfaces.CourseService;
import com.example.e_learning_system.Mapper.CourseMapper;
import com.example.e_learning_system.Repository.CourseModulesRepository;
import com.example.e_learning_system.Repository.CourseRepository;
import com.example.e_learning_system.Repository.ModuleRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final EntityManager entityManager;
    private final CourseModulesRepository courseModulesRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseSummaryDto> getCourses() {
        log.debug("Fetching all courses");

        List<Course> courses = courseRepository.findAll();
        return CourseMapper.fromCourseEntitiesToCourseSummaryDtos(courses);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseSummaryDto> getCourses(CourseFilterDto filterDto, Pageable pageable) {
        log.debug("Fetching courses with filter: {} and pagination", filterDto);

        // Build dynamic query using Criteria API
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Course> query = cb.createQuery(Course.class);
        Root<Course> root = query.from(Course.class);

        List<Predicate> predicates = buildPredicates(cb, root, filterDto);

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Get total count for pagination
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Course> countRoot = countQuery.from(Course.class);
        countQuery.select(cb.count(countRoot));

        List<Predicate> countPredicates = buildPredicates(cb, countRoot, filterDto);
        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }

        long total = entityManager.createQuery(countQuery).getSingleResult();

        // Apply pagination and get results
        List<Course> courses = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<CourseSummaryDto> courseDtos = CourseMapper.fromCourseEntitiesToCourseSummaryDtos(courses);

        return new PageImpl<>(courseDtos, pageable, total);
    }

    @Override
    public CourseDetailsDto createCourse(CreateCourseDto request, Integer createdById) {
        log.info("Creating new course: {} for user: {}", request.getName(), createdById);

        UserEntity creator = userRepository.findById(createdById)
                .orElseThrow(() -> ResourceNotFound.userNotFound(createdById.toString()));

        // Validate business rules
        validateCourseRequest(request);

        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setOneTimePrice(request.getOneTimePrice());
        course.setCurrency(request.getCurrency());
        course.setThumbnail(request.getThumbnail());
        course.setPreviewVideoUrl(request.getPreviewVideoUrl());
        course.setEstimatedDrationInHours(request.getEstimatedDurationInHours());
        course.setActive(request.isActive());
        course.setStatus(request.getStatus());
        course.setDifficultyLevel(request.getDifficultyLevel());
        course.setCreatedBy(creator);
        // Note: isFree is excluded as it's a calculated field (insertable = false, updatable = false)

        Course savedCourse = courseRepository.save(course);

        log.info("Course created successfully with id: {}", savedCourse.getId());
        return CourseMapper.fromCourseEntityToCourseDetailsDto(savedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailsDto getCourseById(Integer id) {
        log.debug("Fetching course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(id.toString()));

        return CourseMapper.fromCourseEntityToCourseDetailsDto(course);
    }

    @Override
    public void updateCourse(UpdateCourseDto updateCourseDto , int courseId) {
        log.info("Updating course with id: {}", courseId);

        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId+""));

        // Validate business rules
        validateUpdateCourseRequest(updateCourseDto);

        // Check if published course can be updated
        if (existingCourse.getStatus() == CourseStatus.PUBLISHED && !canUpdatePublishedCourse(existingCourse)) {
            throw new RuntimeException("Cannot update published course with active enrollments");
        }

        // Update the existing course entity using the mapper
        CourseMapper.fromUpdateCourseDtoToCourseEntity( updateCourseDto , existingCourse);

        courseRepository.save(existingCourse);

        log.info("Course updated successfully: {}", courseId);
    }

    @Override
    public void deleteCourse(Integer id) {
        log.info("Deleting course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(id.toString()));

        // Check if course can be deleted (e.g., no active enrollments)
        if (!canDeleteCourse(course)) {
            throw new RuntimeException("Cannot delete course with active enrollments or dependencies");
        }

        courseRepository.deleteById(id);

        log.info("Course deleted successfully: {}", id);
    }

    @Override
    public void deactivateCourse(Integer id , boolean deactivate) {
        log.info("Deactivating course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(id.toString()));
        if (deactivate){
            course.setActive(false);
            course.setStatus(CourseStatus.DRAFT); // Move back to draft when deactivated
        }else {
            course.setActive(true);
        }


        courseRepository.save(course);

        log.info("Course deactivated successfully: {}", id);
    }

    public void addMoudelToCourse (int courseId , int moduleId , int order ){
        log.info("adding module {} to course {}",moduleId,courseId);


        Course course = courseRepository.findById(courseId).orElseThrow(() -> ResourceNotFound.courseNotFound(courseId+""));
        Optional<Module> module = moduleRepository.findById(moduleId);
        if (module.isEmpty()){
            throw ResourceNotFound.moduleNotFound(moduleId+"");
        }
        Optional<CourseModules> courseModule=  courseModulesRepository.findByCourseIdAndModuleId(courseId, moduleId) ;
        if(courseModule.isPresent()){
            log.warn("Course module {} already exists", moduleId);
        }else {
            if (course.isUniqOrder(order)){
                CourseModules newCourseModule = new CourseModules();
                newCourseModule.setCourse(course);
                newCourseModule.setModule(module.get());
                course.addCourseModules(newCourseModule);
                courseRepository.save(course);
            }else {
                throw new RuntimeException("module order already exists");
            }

        }
    }

    public void removeMoudelFromCourse (int courseId , int moduleId){
        log.info("removing module {} from course {}",moduleId,courseId);
        Optional<CourseModules> courseModule =  courseModulesRepository.findByCourseIdAndModuleId(courseId, moduleId);

        if (courseModule.isPresent()){
            log.info("Course module {} removed", moduleId);
            Course course = courseModule.get().getCourse();
            course.removeCourseModules(courseModule.get());
            courseRepository.save(course);
        }
    }




    // Private helper methods

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Course> root, CourseFilterDto filterDto) {
        List<Predicate> predicates = new ArrayList<>();

        if (filterDto == null) {
            return predicates;
        }

        // Text search filters
        if (filterDto.getName() != null && !filterDto.getName().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")),
                    "%" + filterDto.getName().toLowerCase() + "%"));
        }

        if (filterDto.getDescription() != null && !filterDto.getDescription().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("description")),
                    "%" + filterDto.getDescription().toLowerCase() + "%"));
        }

        // Price range filters
        if (filterDto.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("oneTimePrice"), filterDto.getMinPrice()));
        }

        if (filterDto.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("oneTimePrice"), filterDto.getMaxPrice()));
        }

        // Currency filter
        if (filterDto.getCurrency() != null) {
            predicates.add(cb.equal(root.get("currency"), filterDto.getCurrency()));
        }

        // Duration filters
        if (filterDto.getMinDurationHours() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("estimatedDrationInHours"),
                    filterDto.getMinDurationHours()));
        }

        if (filterDto.getMaxDurationHours() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("estimatedDrationInHours"),
                    filterDto.getMaxDurationHours()));
        }

        // Boolean flags
        if (filterDto.getIsActive() != null) {
            predicates.add(cb.equal(root.get("isActive"), filterDto.getIsActive()));
        }

        if (filterDto.getIsFree() != null) {
            predicates.add(cb.equal(root.get("isFree"), filterDto.getIsFree()));
        }

        // Creator filter
        if (filterDto.getCreatedByUserId() != null) {
            predicates.add(cb.equal(root.get("createdBy").get("id"), filterDto.getCreatedByUserId()));
        }

        // Status filters
        if (filterDto.getStatuses() != null && !filterDto.getStatuses().isEmpty()) {
            predicates.add(root.get("status").in(filterDto.getStatuses()));
        }

        // Difficulty level filters
        if (filterDto.getDifficultyLevels() != null && !filterDto.getDifficultyLevels().isEmpty()) {
            predicates.add(root.get("difficultyLevel").in(filterDto.getDifficultyLevels()));
        }

        return predicates;
    }

    private void validateCourseRequest(CreateCourseDto request) {
        // Validate pricing logic
        if (request.getOneTimePrice() != null && request.getOneTimePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Course price cannot be negative");
        }

        // Validate currency for paid courses
        if (request.getOneTimePrice() != null && request.getOneTimePrice().compareTo(BigDecimal.ZERO) > 0) {
            if (request.getCurrency() == null) {
                throw new IllegalArgumentException("Currency is required for paid courses");
            }
        }

        // Validate estimated duration
        if ( request.getEstimatedDurationInHours() <= 0) {
            throw new IllegalArgumentException("Estimated duration must be positive");
        }

        // Validate required fields
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Course name is required");
        }
    }

    private void validateUpdateCourseRequest(UpdateCourseDto updateCourseDto) {
        // Validate pricing logic
        if (updateCourseDto.getOneTimePrice() != null && updateCourseDto.getOneTimePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Course price cannot be negative");
        }

        // Validate currency for paid courses
        if (updateCourseDto.getOneTimePrice() != null && updateCourseDto.getOneTimePrice().compareTo(BigDecimal.ZERO) > 0) {
            if (updateCourseDto.getCurrency() == null) {
                throw new IllegalArgumentException("Currency is required for paid courses");
            }
        }

        // Validate estimated duration
        if (updateCourseDto.getEstimatedDurationInHours() <= 0) {
            throw new IllegalArgumentException("Estimated duration must be positive");
        }

        // Validate required fields
        if (updateCourseDto.getName() == null || updateCourseDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Course name is required");
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

        // Check if course has modules
        if (course.getCourseModules() == null || course.getCourseModules().isEmpty()) {
            validationErrors.add("Course must have at least one module to be published");
        }

        // Additional business rules for publishing
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            validationErrors.add("Cannot publish an archived course");
        }

        if (!validationErrors.isEmpty()) {
            throw new RuntimeException("Course validation failed: " + String.join(", ", validationErrors));
        }
    }

    // TODO: Implement proper business logic
    private boolean canUpdatePublishedCourse(Course course) {

        return true;
    }

    // TODO: Implement proper business logic
    private boolean canDeleteCourse(Course course) {

        return course.getStatus() == CourseStatus.DRAFT || course.getStatus() == CourseStatus.ARCHIVED;
    }



    


}
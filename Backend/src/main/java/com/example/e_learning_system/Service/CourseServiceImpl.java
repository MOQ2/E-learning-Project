package com.example.e_learning_system.Service;

import com.example.e_learning_system.Config.CourseStatus;
import com.example.e_learning_system.Dto.CourseDtos.*;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.CourseModules;
import com.example.e_learning_system.Entities.Module;
import com.example.e_learning_system.Entities.TagsEntity;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Service.Interfaces.CourseService;
import com.example.e_learning_system.Mapper.CourseMapper;
import com.example.e_learning_system.Repository.CourseModulesRepository;
import com.example.e_learning_system.Repository.CourseRepository;
import com.example.e_learning_system.Repository.ModuleRepository;
import com.example.e_learning_system.Repository.TagsRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.Repository.AttachmentRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.example.e_learning_system.Repository.UserCourseAccessRepository;
import com.example.e_learning_system.Repository.UserFeedbackRepository;
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
    private final TagsRepository tagsRepository;
    private final AttachmentRepository attachmentRepository;
    private final UserCourseAccessRepository userCourseAccessRepository;
    private final UserFeedbackRepository userFeedbackRepository;
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
    @Transactional
    public CourseDetailsDto createCourse(CreateCourseDto request, Integer createdById) {
        log.info("Creating new course: {} for user: {}", request.getName(), createdById);

        UserEntity creator = userRepository.findById(createdById)
                .orElseThrow(() -> ResourceNotFound.userNotFound(createdById.toString()));

        Course course = CourseMapper.fromCreateCourseDtoToCourseEntity(request, creator, tagsRepository, attachmentRepository);
        


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

        CourseDetailsDto details = CourseMapper.fromCourseEntityToCourseDetailsDto(course);
        
        try {
            long enrolled = userCourseAccessRepository.countByCourseIdAndIsActiveTrue(id);
            details.setEnrolledCount((int) enrolled);
        } catch (Exception e) {
            log.warn("Failed to compute enrolled count for course {}: {}", id, e.getMessage());
            details.setEnrolledCount(0);
        }

        try {
            Double avg = userFeedbackRepository.findAverageRatingByCourseId(id);
            details.setAverageRating(avg == null ? 0.0 : avg);
        } catch (Exception e) {
            log.warn("Failed to compute average rating for course {}: {}", id, e.getMessage());
            details.setAverageRating(0.0);
        }
        try {
            Integer count = userFeedbackRepository.findReviewCountByCourseId(id);
            details.setReviewCount(count == null ? 0 : count);
        } catch (Exception e) {
            log.warn("Failed to compute review count for course {}: {}", id, e.getMessage());
            details.setReviewCount(0);
        }

        return details;
    }

    @Override
    public void updateCourse(UpdateCourseDto updateCourseDto , int courseId) {
        log.info("Updating course with id: {}", courseId);

        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId+""));

        // Check if published course can be updated
        if (existingCourse.getStatus() == CourseStatus.PUBLISHED && !canUpdatePublishedCourse(existingCourse)) {
            throw new RuntimeException("Cannot update published course with active enrollments");
        }

        // Update the existing course entity using the mapper
        CourseMapper.fromUpdateCourseDtoToCourseEntity( updateCourseDto , existingCourse, tagsRepository, attachmentRepository);

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

    public void addModuleToCourse (int courseId , int moduleId , int order ){
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
                newCourseModule.setModuleOrder(order);
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

        // Tags filter
        if (filterDto.getTags() != null && !filterDto.getTags().isEmpty()) {
            for (String tagName : filterDto.getTags()) {
                predicates.add(cb.equal(root.join("tags").get("name"), tagName));
            }
        }

        // Category filter
        if (filterDto.getCategories() != null && !filterDto.getCategories().isEmpty()) {
            predicates.add(root.get("category").in(filterDto.getCategories()));
        }


        log.info("Filter DTO: {}", filterDto);
        return predicates;
    }

    // TODO: Implement proper business logic
    private boolean canUpdatePublishedCourse(Course course) {

        return true;
    }

    // TODO: Implement proper business logic
    private boolean canDeleteCourse(Course course) {

        return course.getStatus() == CourseStatus.DRAFT || course.getStatus() == CourseStatus.ARCHIVED;
    }

    public List<TagDto> getAllTags() {
        List<TagsEntity> tags = tagsRepository.findAll();
        List<TagDto> tagDtos = new ArrayList<>();
        for (TagsEntity tag : tags) {
            tagDtos.add(new TagDto( tag.getName(), tag.getDescription(), tag.getColor()));
        }
        return tagDtos;
    }

    @Override
    public void updateModuleOrderInCourse(int courseId, int moduleId, int newOrder) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId + ""));
        CourseModules courseModule = courseModulesRepository.findByCourseIdAndModuleId(courseId, moduleId)
                .orElseThrow(() -> ResourceNotFound.moduleNotFoundInCourse(moduleId + "", courseId + ""));
        if (!course.isUniqOrder(newOrder)) {
            throw new RuntimeException("Module order already exists in the course select a unique order");
        }
        courseModule.setModuleOrder(newOrder);
        courseModulesRepository.save(courseModule);
    }

    @Override
    public void updateModuleOrdersInCourse(int courseId, java.util.List<com.example.e_learning_system.Dto.OrderDtos.IdOrderDto> orders) {
        if (orders == null || orders.isEmpty()) return;

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId + ""));

        // Validate no duplicate orders in payload
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (com.example.e_learning_system.Dto.OrderDtos.IdOrderDto od : orders) {
            if (od.getOrder() == null || od.getOrder() < 0) throw new RuntimeException("Invalid order");
            if (!seen.add(od.getOrder())) throw new RuntimeException("Duplicate order in payload");
        }

        // Apply updates
        for (com.example.e_learning_system.Dto.OrderDtos.IdOrderDto od : orders) {
            int moduleId = od.getId();
            int newOrder = od.getOrder();
            CourseModules cm = courseModulesRepository.findByCourseIdAndModuleId(courseId, moduleId)
                    .orElseThrow(() -> ResourceNotFound.moduleNotFoundInCourse(moduleId + "", courseId + ""));

            // ensure uniqueness among existing ones (excluding current)
            boolean exists = course.getCourseModules().stream()
                    .anyMatch(cmod -> cmod.getModuleOrder() == newOrder && cmod.getModule().getId() != moduleId);
            if (exists) {
                throw new RuntimeException("module order already exists in the course");
            }
            cm.setModuleOrder(newOrder);
            courseModulesRepository.save(cm);
        }
    }

    @Override
    public void removeModuleFromCourse(int courseId, int moduleId) {
        CourseModules courseModule = courseModulesRepository.findByCourseIdAndModuleId(courseId, moduleId)
                .orElseThrow(() -> ResourceNotFound.moduleNotFoundInCourse(moduleId + "", courseId + ""));
        courseModulesRepository.delete(courseModule);

    }
}
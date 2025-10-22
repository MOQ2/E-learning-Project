package com.example.e_learning_system.Service;

import com.example.e_learning_system.Config.CourseStatus;

import com.example.e_learning_system.Dto.CourseDtos.*;
import com.example.e_learning_system.Entities.Attachment;
import com.example.e_learning_system.Entities.CourseModules;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.Module;
import com.example.e_learning_system.Entities.TagsEntity;
import com.example.e_learning_system.Entities.TagsEntity;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Service.Interfaces.CourseService;
import com.example.e_learning_system.Mapper.CourseMapper;
import com.example.e_learning_system.Dto.OrderDtos.IdOrderDto;
import com.example.e_learning_system.Repository.CourseModulesRepository;
import com.example.e_learning_system.Repository.CourseRepository;
import com.example.e_learning_system.Repository.ModuleRepository;
import com.example.e_learning_system.Repository.TagsRepository;
import com.example.e_learning_system.Repository.TagsRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.Repository.AttachmentRepository;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.example.e_learning_system.Repository.UserCourseAccessRepository;
import com.example.e_learning_system.Repository.UserFeedbackRepository;
import com.example.e_learning_system.Service.Interfaces.RagService;
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
    private final RagService ragService;
    @Value("${app.public-base-url:http://localhost:5000}")
    private String publicBaseUrl;
    @Override
    @Transactional(readOnly = true)
    public List<CourseSummaryDto> getCourses() {
        log.debug("Fetching all courses");

        List<Course> courses = courseRepository.findAll();
        List<CourseSummaryDto> summaries = CourseMapper.fromCourseEntitiesToCourseSummaryDtos(courses);
        enrichCourseSummaries(courses, summaries);
        return summaries;
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
        boolean filterByTags = filterDto != null && filterDto.getTags() != null && !filterDto.getTags().isEmpty();

        if (filterByTags) {
            query.distinct(true);
        }

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : pageable.getSort()) {
                try {
                    Path<?> path = root.get(sortOrder.getProperty());
                    orders.add(sortOrder.isAscending() ? cb.asc(path) : cb.desc(path));
                } catch (IllegalArgumentException ex) {
                    log.warn("Ignoring unsupported sort property: {}", sortOrder.getProperty());
                }
            }
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }
        }

        // Get total count for pagination
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Course> countRoot = countQuery.from(Course.class);
        if (filterByTags) {
            countQuery.select(cb.countDistinct(countRoot));
        } else {
            countQuery.select(cb.count(countRoot));
        }

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
    enrichCourseSummaries(courses, courseDtos);

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

        // Index the course in RAG service for intelligent recommendations
        try {
            indexCourseInRag(savedCourse);
        } catch (Exception e) {
            log.error("Failed to index course {} in RAG service: {}", savedCourse.getId(), e.getMessage());
            // Don't fail course creation if RAG indexing fails
        }

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
        details.setEnrolledCount(resolveEnrolledCount(id));
        details.setAverageRating(resolveAverageRating(id));
        details.setReviewCount(resolveReviewCount(id));
        details.setThumbnailUrl(buildAttachmentUrl(course.getThumbnail()));

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

        Course updatedCourse = courseRepository.save(existingCourse);

        // Re-index the updated course in RAG service
        try {
            indexCourseInRag(updatedCourse);
        } catch (Exception e) {
            log.error("Failed to re-index course {} in RAG service: {}", courseId, e.getMessage());
            // Don't fail course update if RAG indexing fails
        }

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




    private void enrichCourseSummaries(List<Course> courses, List<CourseSummaryDto> summaries) {
        if (courses == null || summaries == null || summaries.isEmpty()) {
            return;
        }

        int limit = Math.min(courses.size(), summaries.size());
        for (int i = 0; i < limit; i++) {
            Course course = courses.get(i);
            CourseSummaryDto dto = summaries.get(i);
            if (course == null || dto == null) {
                continue;
            }

            dto.setThumbnailUrl(buildAttachmentUrl(course.getThumbnail()));
            dto.setCategory(course.getCategory());
            dto.setEnrolledCount(resolveEnrolledCount(course.getId()));
            dto.setAverageRating(resolveAverageRating(course.getId()));
            dto.setReviewCount(resolveReviewCount(course.getId()));
        }
    }

    private int resolveEnrolledCount(Integer courseId) {
        if (courseId == null) {
            return 0;
        }
        try {
            long enrolled = userCourseAccessRepository.countByCourseIdAndIsActiveTrue(courseId);
            return (int) enrolled;
        } catch (Exception e) {
            log.warn("Failed to compute enrolled count for course {}: {}", courseId, e.getMessage());
            return 0;
        }
    }

    private double resolveAverageRating(Integer courseId) {
        if (courseId == null) {
            return 0.0;
        }
        try {
            Double avg = userFeedbackRepository.findAverageRatingByCourseId(courseId);
            return avg == null ? 0.0 : avg;
        } catch (Exception e) {
            log.warn("Failed to compute average rating for course {}: {}", courseId, e.getMessage());
            return 0.0;
        }
    }

    private int resolveReviewCount(Integer courseId) {
        if (courseId == null) {
            return 0;
        }
        try {
            Integer count = userFeedbackRepository.findReviewCountByCourseId(courseId);
            return count == null ? 0 : count;
        } catch (Exception e) {
            log.warn("Failed to compute review count for course {}: {}", courseId, e.getMessage());
            return 0;
        }
    }

    private String buildAttachmentUrl(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        int attachmentId = attachment.getId();
        if (attachmentId <= 0) {
            return null;
        }
        String base = (publicBaseUrl == null) ? "" : publicBaseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/api/attachments/" + attachmentId + "/download";
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
            Join<Course, TagsEntity> tagJoin = root.join("tags", JoinType.LEFT);
            predicates.add(tagJoin.get("name").in(filterDto.getTags()));
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
        updateModuleOrdersInCourse(courseId, Collections.singletonList(new IdOrderDto(moduleId, newOrder)));
    }

    @Override
    public void updateModuleOrdersInCourse(int courseId, List<IdOrderDto> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId + ""));

        List<CourseModules> courseModules = courseModulesRepository.findByCourse(course);
        if (courseModules.isEmpty()) {
            return;
        }

        Map<Integer, CourseModules> courseModuleByModuleId = courseModules.stream()
                .collect(Collectors.toMap(cm -> cm.getModule().getId(), cm -> cm));

        Map<Integer, Integer> requestedOrders = new HashMap<>();
        for (IdOrderDto od : orders) {
            if (od.getId() == null) {
                throw new RuntimeException("Module id is required");
            }
            if (!courseModuleByModuleId.containsKey(od.getId())) {
                throw ResourceNotFound.moduleNotFoundInCourse(od.getId() + "", courseId + "");
            }
            if (od.getOrder() == null || od.getOrder() < 0) {
                throw new RuntimeException("Invalid order value");
            }
            Integer previous = requestedOrders.putIfAbsent(od.getId(), od.getOrder());
            if (previous != null && !previous.equals(od.getOrder())) {
                throw new RuntimeException("Conflicting orders supplied for the same module");
            }
        }

        Set<Integer> finalOrders = new HashSet<>();
        for (CourseModules courseModule : courseModules) {
            int moduleId = courseModule.getModule().getId();
            int newOrder = requestedOrders.getOrDefault(moduleId, courseModule.getModuleOrder());
            if (!finalOrders.add(newOrder)) {
                throw new RuntimeException("Duplicate module order detected");
            }
            courseModule.setModuleOrder(newOrder);
        }

        courseModulesRepository.saveAll(courseModules);
    }

    @Override
    public void removeModuleFromCourse(int courseId, int moduleId) {
        CourseModules courseModule = courseModulesRepository.findByCourseIdAndModuleId(courseId, moduleId)
                .orElseThrow(() -> ResourceNotFound.moduleNotFoundInCourse(moduleId + "", courseId + ""));
        courseModulesRepository.delete(courseModule);

    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseSearchResultDto> searchCourses(String query) {
        log.debug("Searching courses with query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String trimmedQuery = query.trim();
        List<Course> courses = courseRepository.searchCourses(trimmedQuery);

        return courses.stream().map(course -> {
            // Determine match type
            String matchType = determineMatchType(course, trimmedQuery);

            // Count lessons/videos
            Integer lessonCount = course.getCourseModules() != null
                    ? course.getCourseModules().stream()
                    .mapToInt(cm -> cm.getModule().getModuleVideos() != null ? cm.getModule().getModuleVideos().size() : 0)
                    .sum()
                    : 0;

            // Build tags set
            Set<TagDto> tagDtos = new HashSet<>();
            if (course.getTags() != null) {
                for (TagsEntity tag : course.getTags()) {
                    TagDto tagDto = new TagDto();
                    tagDto.setName(tag.getName());
                    tagDto.setColor(tag.getColor());
                    tagDto.setDescription(tag.getDescription());
                    tagDtos.add(tagDto);
                }
            }

            return CourseSearchResultDto.builder()
                    .id(course.getId())
                    .name(course.getName())
                    .description(course.getDescription())
                    .thumbnailUrl(course.getThumbnail() != null
                            ? publicBaseUrl + "/api/attachments/" + course.getThumbnail().getId() + "/download"
                            : null)
                    .instructor(course.getCreatedBy() != null
                            ? course.getCreatedBy().getName()
                            : "Unknown")
                    .estimatedDurationInHours(course.getEstimatedDrationInHours())
                    .lessonCount(lessonCount)
                    .oneTimePrice(course.getOneTimePrice())
                    .currency(course.getCurrency() != null ? course.getCurrency().name() : null)
                    .category(course.getCategory())
                    .tags(tagDtos)
                    .matchType(matchType)
                    .build();
        }).collect(Collectors.toList());
    }

    private String determineMatchType(Course course, String query) {
        String lowerQuery = query.toLowerCase();

        // Check for exact match in title
        if (course.getName().toLowerCase().equals(lowerQuery)) {
            return "title_exact";
        }

        // Check for match in title
        if (course.getName().toLowerCase().contains(lowerQuery)) {
            return "title";
        }

        // Check for match in instructor name
        if (course.getCreatedBy() != null &&
                course.getCreatedBy().getName() != null &&
                course.getCreatedBy().getName().toLowerCase().contains(lowerQuery)) {
            return "instructor";
        }

        // Check for match in tags
        if (course.getTags() != null && course.getTags().stream()
                .anyMatch(tag -> tag.getName().toLowerCase().contains(lowerQuery))) {
            return "tag";
        }

        // Check for match in category
        if (course.getCategory() != null &&
                course.getCategory().name().toLowerCase().contains(lowerQuery)) {
            return "category";
        }

        // Check for match in description
        if (course.getDescription() != null &&
                course.getDescription().toLowerCase().contains(lowerQuery)) {
            return "description";
        }

        return "other";
    }

    /**
     * Index a course in the RAG service for intelligent recommendations
     * This method is called when courses are created or updated
     */
    private void indexCourseInRag(Course course) {
        try {
            log.debug("Indexing course {} in RAG service", course.getId());
            
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("name", course.getName());
            courseData.put("description", course.getDescription());
            
            if (course.getCategory() != null) {
                courseData.put("category", course.getCategory().name());
            }
            
            if (course.getDifficultyLevel() != null) {
                courseData.put("difficulty_level", course.getDifficultyLevel().name());
            }
            
            // Add tags
            if (course.getTags() != null && !course.getTags().isEmpty()) {
                List<String> tagNames = course.getTags().stream()
                    .map(TagsEntity::getName)
                    .collect(Collectors.toList());
                courseData.put("tags", tagNames);
            }
            
            // Add modules information
            if (course.getCourseModules() != null && !course.getCourseModules().isEmpty()) {
                List<Map<String, Object>> modules = course.getCourseModules().stream()
                    .map(courseModule -> {
                        Map<String, Object> moduleInfo = new HashMap<>();
                        Module module = courseModule.getModule();
                        if (module != null) {
                            moduleInfo.put("title", module.getName()); 
                            moduleInfo.put("description", module.getDescription());
                        }
                        return moduleInfo;
                    })
                    .collect(Collectors.toList());
                courseData.put("modules", modules);
            }
            
            // Additional course metadata
            if (course.getOneTimePrice() != null) {
                courseData.put("price", course.getOneTimePrice());
            }
            
            if (course.getCurrency() != null) {
                courseData.put("currency", course.getCurrency().name());
            }
            
            // Call RAG service to index the course
            boolean success = ragService.indexCourse(Long.valueOf(course.getId()), courseData);
            
            if (success) {
                log.info("Successfully indexed course {} in RAG service", course.getId());
            } else {
                log.warn("Failed to index course {} in RAG service", course.getId());
            }
            
        } catch (Exception e) {
            log.error("Error indexing course {} in RAG service: {}", course.getId(), e.getMessage(), e);
            throw e;  // Re-throw to allow caller to handle gracefully
        }
    }
}
package com.example.e_learning_system.Mapper;


import com.example.e_learning_system.Dto.CourseDtos.CourseDetailsDto;
import com.example.e_learning_system.Dto.CourseDtos.CourseModuleDto;
import com.example.e_learning_system.Dto.CourseDtos.CourseSummaryDto;
import com.example.e_learning_system.Dto.CourseDtos.CreateCourseDto;
import com.example.e_learning_system.Dto.CourseDtos.TagDto;
import com.example.e_learning_system.Dto.CourseDtos.TagDto;
import com.example.e_learning_system.Dto.CourseDtos.UpdateCourseDto;
import com.example.e_learning_system.Entities.Attachment;
import com.example.e_learning_system.Entities.Course;
import com.example.e_learning_system.Entities.CourseModules;
import com.example.e_learning_system.Entities.TagsEntity;
import com.example.e_learning_system.Entities.TagsEntity;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Repository.AttachmentRepository;
import com.example.e_learning_system.Repository.TagsRepository;
import com.example.e_learning_system.excpetions.BaseException;
import com.fasterxml.jackson.databind.JsonSerializable.Base;
import com.example.e_learning_system.Config.Category;
import java.math.BigDecimal;
import java.util.Collections;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class CourseMapper {
    private CourseMapper() {
        // private constructor to prevent instantiation
    }

    // ====== ENTITY -> DTO ======

    /**
     * Maps from Course entity to CourseDetailsDto
     */
    public static CourseDetailsDto fromCourseEntityToCourseDetailsDto(Course course) {
        if (course == null) {
            return null;
        }

        BigDecimal monthly = course.getSubscriptionPriceMonthly();
        BigDecimal threeMonths = course.getSubscriptionPrice3Months();
        BigDecimal sixMonths = course.getSubscriptionPrice6Months();
        boolean hasSubscriptionPricing = (monthly != null && monthly.compareTo(BigDecimal.ZERO) > 0)
                || (threeMonths != null && threeMonths.compareTo(BigDecimal.ZERO) > 0)
                || (sixMonths != null && sixMonths.compareTo(BigDecimal.ZERO) > 0);

        return CourseDetailsDto.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .oneTimePrice(course.getOneTimePrice())
                .subscriptionPriceMonthly(monthly)
                .subscriptionPrice3Months(threeMonths)
                .subscriptionPrice6Months(sixMonths)
                .allowsSubscription(Boolean.TRUE.equals(course.getAllowsSubscription()) || hasSubscriptionPricing)
                .currency(course.getCurrency())
                .thumbnail(course.getThumbnail() != null ? course.getThumbnail().getId() : null)
                .estimatedDurationInHours(course.getEstimatedDrationInHours())
                .status(course.getStatus())
                .difficultyLevel(course.getDifficultyLevel())
                .isActive(course.isActive())
                .modules(fromCourseModulesToCourseModuleDtos(course.getCourseModules()))
                .tags(convertTagsEntityToTagDtos(course.getTags()))
                .instructor(course.getCreatedBy() != null ? course.getCreatedBy().getName() : null)
                .category(course.getCategory())
                .build();
    }

    /**
     * Maps from Course entity to CourseSummaryDto
     */
    public static CourseSummaryDto fromCourseEntityToCourseSummaryDto(Course course) {
        if (course == null) {
            return null;
        }

        return CourseSummaryDto.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .status(course.getStatus())
                .difficultyLevel(course.getDifficultyLevel())
                .isActive(course.isActive())
                .oneTimePrice(course.getOneTimePrice())
                .currency(course.getCurrency())
                .tags(convertTagsEntityToTagDtos(course.getTags()))
                .thumbnail(course.getThumbnail() != null ? course.getThumbnail().getId() : null)
                .instructor(course.getCreatedBy() != null ? course.getCreatedBy().getName() : null)
                .estimatedDurationInHours(course.getEstimatedDrationInHours())
                .category(course.getCategory())
                .build();
    }

    /**
     * Maps a list of Course entities to a list of CourseDetailsDto
     */
    public static List<CourseDetailsDto> fromCourseEntitiesToCourseDetailsDtos(List<Course> courses) {
        if (courses == null) {
            return Collections.emptyList();
        }

        return courses.stream()
                .map(CourseMapper::fromCourseEntityToCourseDetailsDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps a list of Course entities to a list of CourseSummaryDto
     */
    public static List<CourseSummaryDto> fromCourseEntitiesToCourseSummaryDtos(List<Course> courses) {
        if (courses == null) {
            return Collections.emptyList();
        }

        return courses.stream()
                .map(CourseMapper::fromCourseEntityToCourseSummaryDto)
                .collect(Collectors.toList());
    }

    // ====== DTO -> ENTITY ======

    /**
     * Maps from CreateCourseDto to Course entity
     */
    public static Course fromCreateCourseDtoToCourseEntity(CreateCourseDto createCourseDto, UserEntity createdBy, TagsRepository tagsRepository, AttachmentRepository attachmentRepository) {
        if (createCourseDto == null) {
            return null;
        }

        
        Set<TagsEntity> tags = createCourseDto.getTags().stream()
            .filter(tagName -> tagName != null && !tagName.trim().isEmpty())
            .map(tagName -> {
                String trimmedName = tagName.trim();
                TagsEntity existing = tagsRepository.findByName(trimmedName);
                if (existing != null) {
                    return existing;
                } else {
                TagsEntity newTag = new TagsEntity();
                newTag.setName(trimmedName);
                newTag.setColor("#cbdab8");
                return newTag;
                }
            })
            .collect(Collectors.toSet());
        Attachment thumbnail = null;
        if (createCourseDto.getThumbnail() != null) {
            thumbnail = attachmentRepository.findById(createCourseDto.getThumbnail()).orElseThrow(() -> new ResourceNotFoundException("Thumbnail not found for ID: " + createCourseDto.getThumbnail()));
        }

        

        return Course.builder()
                .name(createCourseDto.getName())
                .description(createCourseDto.getDescription())
                .oneTimePrice(createCourseDto.getOneTimePrice())
                .subscriptionPriceMonthly(createCourseDto.getSubscriptionPriceMonthly())
                .subscriptionPrice3Months(createCourseDto.getSubscriptionPrice3Months())
                .subscriptionPrice6Months(createCourseDto.getSubscriptionPrice6Months())
                .allowsSubscription(createCourseDto.getAllowsSubscription())
                .currency(createCourseDto.getCurrency())
                .thumbnail(thumbnail)
                .category(createCourseDto.getCategory())
                .estimatedDrationInHours(createCourseDto.getEstimatedDurationInHours())
                .status(createCourseDto.getStatus())
                .difficultyLevel(createCourseDto.getDifficultyLevel())
                .isActive(createCourseDto.isActive())
                .createdBy(createdBy)
                .tags(tags)
                .tags(tags)
                .build();
    }

    /**
     * Maps from UpdateCourseDto to Course entity
     */
    public static Course fromUpdateCourseDtoToCourseEntity(UpdateCourseDto updateCourseDto, Course existingCourse, TagsRepository tagsRepository, AttachmentRepository attachmentRepository) {
        if (updateCourseDto == null ||  existingCourse == null ) {
            return null;
        }

        if (updateCourseDto.getName() != null  ) {
            existingCourse.setName(updateCourseDto.getName());
        }
        if (updateCourseDto.getDescription() != null ) {
            existingCourse.setDescription(updateCourseDto.getDescription());
        }
        if (updateCourseDto.getOneTimePrice() != null ) {
            existingCourse.setOneTimePrice(updateCourseDto.getOneTimePrice());
        }
        if (updateCourseDto.getSubscriptionPriceMonthly() != null ) {
            existingCourse.setSubscriptionPriceMonthly(updateCourseDto.getSubscriptionPriceMonthly());
        }
        if (updateCourseDto.getSubscriptionPrice3Months() != null ) {
            existingCourse.setSubscriptionPrice3Months(updateCourseDto.getSubscriptionPrice3Months());
        }
        if (updateCourseDto.getSubscriptionPrice6Months() != null ) {
            existingCourse.setSubscriptionPrice6Months(updateCourseDto.getSubscriptionPrice6Months());
        }
        if (updateCourseDto.getAllowsSubscription() != null ) {
            existingCourse.setAllowsSubscription(updateCourseDto.getAllowsSubscription());
        }
        if (updateCourseDto.getCurrency() != null ) {
            existingCourse.setCurrency(updateCourseDto.getCurrency());
        }
        if (updateCourseDto.getCategory() != null ) {
            existingCourse.setCategory(updateCourseDto.getCategory());
        }
        if (updateCourseDto.getThumbnail() != null ) {
            Attachment thumbnail = attachmentRepository.findById(updateCourseDto.getThumbnail()).orElse(null);
            existingCourse.setThumbnail(thumbnail);
        }
        if (updateCourseDto.getEstimatedDurationInHours() != null ) {
            existingCourse.setEstimatedDrationInHours(updateCourseDto.getEstimatedDurationInHours());
        }
        if (updateCourseDto.getStatus() != null ) {
            existingCourse.setStatus(updateCourseDto.getStatus());
        }
        if (updateCourseDto.getDifficultyLevel() != null ) {
            existingCourse.setDifficultyLevel(updateCourseDto.getDifficultyLevel());
        }
        if (updateCourseDto.getIsActive() != null ) {
            existingCourse.setActive(updateCourseDto.getIsActive());
        }
        if (updateCourseDto.getTags() != null && !updateCourseDto.getTags().isEmpty()) {
            Set<TagsEntity> tags = updateCourseDto.getTags().stream()
                    .map(tag -> tagsRepository.findByName(tag))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            existingCourse.setTags(tags);
        }
        if (updateCourseDto.getTags() != null && !updateCourseDto.getTags().isEmpty()) {
            Set<TagsEntity> tags = updateCourseDto.getTags().stream()
                    .map(tag -> tagsRepository.findByName(tag))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            existingCourse.setTags(tags);
        }
        return existingCourse;
    }

    /**
     * Maps from CourseDetailsDto to Course entity
     */
    public static Course fromCourseDetailsDtoToCourseEntity(CourseDetailsDto courseDetailsDto, TagsRepository tagsRepository, AttachmentRepository attachmentRepository) {
        if (courseDetailsDto == null) {
            return null;
        }

        Set<TagsEntity> tags = courseDetailsDto.getTags().stream()
                .map(tagDto -> tagsRepository.findByName(tagDto.getName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        return Course.builder()
                .id(courseDetailsDto.getId())
                .name(courseDetailsDto.getName())
                .description(courseDetailsDto.getDescription())
                .oneTimePrice(courseDetailsDto.getOneTimePrice())
                .currency(courseDetailsDto.getCurrency())
                .thumbnail(courseDetailsDto.getThumbnail() != null ? attachmentRepository.findById(courseDetailsDto.getThumbnail()).orElse(null) : null)
                .estimatedDrationInHours(courseDetailsDto.getEstimatedDurationInHours())
                .status(courseDetailsDto.getStatus())
                .difficultyLevel(courseDetailsDto.getDifficultyLevel())
                .isActive(courseDetailsDto.isActive())
                .tags(tags)
                .tags(tags)
                .build();
    }

    // ====== UPDATE OPERATIONS ======

    /**
     * Updates an existing Course entity with data from CreateCourseDto
     */
    public static void updateCourseEntityFromCreateCourseDto(Course existingCourse, CreateCourseDto createCourseDto, TagsRepository tagsRepository, AttachmentRepository attachmentRepository) {
        if (existingCourse == null || createCourseDto == null) {
            return;
        }



        Set<TagsEntity> tags = createCourseDto.getTags().stream()
                .map(tag -> tagsRepository.findByName(tag))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        existingCourse.setName(createCourseDto.getName());
        existingCourse.setDescription(createCourseDto.getDescription());
        existingCourse.setOneTimePrice(createCourseDto.getOneTimePrice());
        existingCourse.setCurrency(createCourseDto.getCurrency());
        existingCourse.setThumbnail(createCourseDto.getThumbnail() != null ? attachmentRepository.findById(createCourseDto.getThumbnail()).orElse(null) : null);
        existingCourse.setEstimatedDrationInHours(createCourseDto.getEstimatedDurationInHours());
        existingCourse.setStatus(createCourseDto.getStatus());
        existingCourse.setDifficultyLevel(createCourseDto.getDifficultyLevel());
        existingCourse.setActive(createCourseDto.isActive());
        existingCourse.setTags(tags);
        existingCourse.setTags(tags);
    }



    // ====== HELPER METHODS ======

    /**
     * Helper method to map CourseModules to CourseModuleDtos
     */
    private static List<CourseModuleDto> fromCourseModulesToCourseModuleDtos(Set<CourseModules> courseModules) {
        if (courseModules == null || courseModules.isEmpty()) {
            return Collections.emptyList();
        }

        return courseModules.stream()
                .map(CourseMapper::fromCourseModuleToCourseModuleDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map single CourseModules to CourseModuleDto
     */
    private static CourseModuleDto fromCourseModuleToCourseModuleDto(CourseModules courseModule) {
        if (courseModule == null || courseModule.getModule() == null) {
            return null;
        }

        CourseModuleDto courseModuleDto = new CourseModuleDto();
        courseModuleDto.setModuleOrder(courseModule.getModuleOrder());
        courseModuleDto.setModule(ModuleMapper.fromModuletoModuleSummaryDto(courseModule.getModule()));

        return courseModuleDto;
    }



    private static Set<TagDto> convertTagsEntityToTagDtos(Set<TagsEntity> tagsEntities) {
        if (tagsEntities == null || tagsEntities.isEmpty()) {
            return Collections.emptySet();
        }

        return tagsEntities.stream()
                .map(tagEntity -> new TagDto(tagEntity.getName(), tagEntity.getDescription(), tagEntity.getColor().toString()))
                .collect(Collectors.toSet());
    }
}
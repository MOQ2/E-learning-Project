package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.MyCourseDtos.MyCoursesResponseDto;
import com.example.e_learning_system.Dto.MyCourseDtos.TeacherCourseDto;
import com.example.e_learning_system.Dto.MyCourseDtos.TeacherStatsDto;
import com.example.e_learning_system.Entities.*;
import com.example.e_learning_system.Repository.*;
import com.example.e_learning_system.Service.Interfaces.MyCoursesService;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MyCoursesServiceImpl implements MyCoursesService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseAccessRepository userCourseAccessRepository;
    private final CourseModulesRepository courseModulesRepository;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final SimplePaymentRepository simplePaymentRepository;
    private final ModuleVideosRepository moduleVideosRepository;

    @Value("${app.public-base-url:http://localhost:5000}")
    private String publicBaseUrl;

    @Override
    public MyCoursesResponseDto getMyCoursesDashboard(Integer teacherId) {
        log.info("Getting My Courses dashboard for teacher {}", teacherId);

        // Verify user exists
        userRepository.findById(teacherId)
                .orElseThrow(() -> ResourceNotFound.userNotFound(teacherId.toString()));

        TeacherStatsDto stats = getTeacherStats(teacherId);
        List<TeacherCourseDto> courses = getTeacherCourses(teacherId);
        List<TeacherCourseDto> recentlyUpdated = getRecentlyUpdatedCourses(teacherId, 5);
        List<TeacherCourseDto> topPerforming = getTopPerformingCourses(teacherId, 5);

        return MyCoursesResponseDto.builder()
                .stats(stats)
                .courses(courses)
                .recentlyUpdatedCourses(recentlyUpdated)
                .topPerformingCourses(topPerforming)
                .build();
    }

    @Override
    public List<TeacherCourseDto> getTeacherCourses(Integer teacherId) {
        log.info("Getting all courses for teacher {}", teacherId);

        UserEntity teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> ResourceNotFound.userNotFound(teacherId.toString()));

        List<Course> courses = courseRepository.findByCreatedBy(teacher);

        return courses.stream()
                .map(this::buildTeacherCourseDto)
                .sorted(Comparator.comparing(TeacherCourseDto::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public TeacherStatsDto getTeacherStats(Integer teacherId) {
        log.info("Getting teacher stats for teacher {}", teacherId);

        UserEntity teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> ResourceNotFound.userNotFound(teacherId.toString()));

        List<Course> courses = courseRepository.findByCreatedBy(teacher);

        int totalCourses = courses.size();
        int publishedCourses = (int) courses.stream().filter(Course::isActive).count();
        int draftCourses = (int) courses.stream().filter(c -> !c.isActive()).count();

        int totalStudents = 0;
        int activeStudents = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal monthlyRevenue = BigDecimal.ZERO;
        double totalEngagement = 0.0;
        int totalReviews = 0;
        int totalModules = 0;
        int totalVideos = 0;
        int totalQuizzes = 0;

        LocalDateTime oneMonthAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);

        for (Course course : courses) {
            // Get enrollments
            List<UserCourseAccess> accesses = userCourseAccessRepository
                    .findByCourseIdAndIsActiveTrue(course.getId());
            totalStudents += accesses.size();

            // Count active students (accessed in last 30 days)
            activeStudents += (int) accesses.stream()
                    .filter(access -> access.getUpdatedAt() != null && 
                            access.getUpdatedAt().isAfter(oneMonthAgo))
                    .count();

            // Calculate revenue
            List<SimplePayment> payments = simplePaymentRepository.findByCourseId(course.getId());
            for (SimplePayment payment : payments) {
                BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
                totalRevenue = totalRevenue.add(amount);

                if (payment.getCreatedAt() != null && payment.getCreatedAt().isAfter(oneMonthAgo)) {
                    monthlyRevenue = monthlyRevenue.add(amount);
                }
            }

            // Count content
            List<CourseModules> courseModules = courseModulesRepository
                    .findByCourseAndIsActive(course, true);
            totalModules += courseModules.size();

            for (CourseModules cm : courseModules) {
                com.example.e_learning_system.Entities.Module module = cm.getModule();
                if (module != null) {
                    // Count videos in module
                    List<ModuleVideos> moduleVideos = moduleVideosRepository
                            .findByModuleAndIsActive(module, true);
                    totalVideos += moduleVideos.size();

                    // Count quizzes for module
                    // Note: Quizzes are related to course, not module in current schema
                }
            }

            // Count quizzes for course
            List<QuizEntity> quizzes = quizRepository.findAll().stream()
                    .filter(q -> q.getCourse() != null && q.getCourse().getId() == course.getId())
                    .filter(QuizEntity::getIsActive)
                    .collect(Collectors.toList());
            totalQuizzes += quizzes.size();

            // Get feedback
            Double avgRating = userFeedbackRepository.findAverageRatingByCourseId(course.getId());
            Integer reviewCount = userFeedbackRepository.findReviewCountByCourseId(course.getId());

            if (avgRating != null && avgRating > 0) {
                totalEngagement += avgRating;
            }
            totalReviews += (reviewCount != null ? reviewCount : 0);
        }

        double averageRating = totalCourses > 0 ? totalEngagement / totalCourses : 0.0;

        return TeacherStatsDto.builder()
                .totalCourses(totalCourses)
                .publishedCourses(publishedCourses)
                .draftCourses(draftCourses)
                .totalStudents(totalStudents)
                .activeStudents(activeStudents)
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .totalModules(totalModules)
                .totalLessons(totalVideos)
                .totalQuizzes(totalQuizzes)
                .totalCompletions((int) (totalStudents * calcOverallCompletionRate(teacherId) / 100.0))
                .averageCompletionRate(calcOverallCompletionRate(teacherId))
                .build();
    }

    @Override
    public List<TeacherCourseDto> getRecentlyUpdatedCourses(Integer teacherId, int limit) {
        log.info("Getting recently updated courses for teacher {}", teacherId);

        return getTeacherCourses(teacherId).stream()
                .sorted(Comparator.comparing(TeacherCourseDto::getUpdatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherCourseDto> getTopPerformingCourses(Integer teacherId, int limit) {
        log.info("Getting top performing courses for teacher {}", teacherId);

        return getTeacherCourses(teacherId).stream()
                .sorted(Comparator.comparing(TeacherCourseDto::getTotalEnrollments).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public TeacherCourseDto getCourseDetails(Integer teacherId, Integer courseId) {
        log.info("Getting course details for teacher {} and course {}", teacherId, courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId.toString()));

        if (course.getCreatedBy().getId() != teacherId) {
            throw new ResourceNotFound("Course", courseId.toString());
        }

        return buildTeacherCourseDto(course);
    }

    private TeacherCourseDto buildTeacherCourseDto(Course course) {
        TeacherCourseDto.TeacherCourseDtoBuilder builder = TeacherCourseDto.builder()
                .courseId(course.getId())
                .courseName(course.getName())
                .description(course.getDescription())
                .isPublished(course.isActive())
                .isActive(course.isActive())
                .oneTimePrice(course.getOneTimePrice())
                .subscriptionPriceMonthly(course.getSubscriptionPriceMonthly())
                .allowsSubscription(course.getAllowsSubscription())
                .isFree(course.isFree())
                .category(course.getCategory())
                .difficultyLevel(course.getDifficultyLevel())
                .status(course.getStatus())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt());

        // Get enrollments
        List<UserCourseAccess> accesses = userCourseAccessRepository
                .findByCourseIdAndIsActiveTrue(course.getId());
        int totalEnrollments = accesses.size();

        // Count active enrollments (accessed in last 30 days)
        LocalDateTime oneMonthAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        int activeEnrollments = (int) accesses.stream()
                .filter(access -> access.getUpdatedAt() != null && 
                        access.getUpdatedAt().isAfter(oneMonthAgo))
                .count();

        // Calculate average progress
        double totalProgress = 0.0;
        int completedCourses = 0;
        for (UserCourseAccess access : accesses) {
            double progress = calculateCourseProgressForStudent(access.getUser().getId(), course.getId());
            totalProgress += progress;
            if (progress >= 100.0) {
                completedCourses++;
            }
        }
        double averageProgress = totalEnrollments > 0 ? totalProgress / totalEnrollments : 0.0;

        // Get ratings
        Double avgRating = userFeedbackRepository.findAverageRatingByCourseId(course.getId());
        Integer reviewCount = userFeedbackRepository.findReviewCountByCourseId(course.getId());

        // Calculate revenue
        List<SimplePayment> payments = simplePaymentRepository.findByCourseId(course.getId());
        BigDecimal totalRevenue = payments.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get thumbnail URL
        String thumbnailUrl = null;
        if (course.getThumbnail() != null) {
            Map<String, Object> metadata = course.getThumbnail().getMetadata();
            if (metadata != null && metadata.containsKey("key")) {
                thumbnailUrl = publicBaseUrl + "/api/attachments/download/" + 
                        metadata.get("key").toString();
            }
        }

        // Determine if course has recent activity
        boolean hasRecentActivity = accesses.stream()
                .anyMatch(access -> access.getUpdatedAt() != null && 
                        access.getUpdatedAt().isAfter(oneMonthAgo));

        // Get content count
        List<CourseModules> courseModules = courseModulesRepository
                .findByCourseAndIsActive(course, true);
        int moduleCount = courseModules.size();

        int videoCount = 0;
        int quizCount = 0;
        for (CourseModules cm : courseModules) {
            com.example.e_learning_system.Entities.Module module = cm.getModule();
            if (module != null) {
                List<ModuleVideos> moduleVideos = moduleVideosRepository
                        .findByModuleAndIsActive(module, true);
                videoCount += moduleVideos.size();
            }
        }

        // Count quizzes for course
        quizCount = (int) quizRepository.findAll().stream()
                .filter(q -> q.getCourse() != null && q.getCourse().getId() == course.getId())
                .filter(QuizEntity::getIsActive)
                .count();

        return builder
                .thumbnailUrl(thumbnailUrl)
                .totalEnrollments(totalEnrollments)
                .activeEnrollments(activeEnrollments)
                .averageProgress(averageProgress)
                .completions(completedCourses)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(reviewCount != null ? reviewCount : 0)
                .totalRevenue(totalRevenue)
                .totalModules(moduleCount)
                .totalLessons(videoCount)
                .totalQuizzes(quizCount)
                .totalDurationMinutes(course.getEstimatedDrationInHours() * 60)
                .recentEnrollments30Days(hasRecentActivity ? activeEnrollments : 0)
                .build();
    }

    private double calculateCourseProgressForStudent(Integer userId, Integer courseId) {
        // Get all modules for the course
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return 0.0;

        List<CourseModules> courseModules = courseModulesRepository
                .findByCourseAndIsActive(course, true);

        if (courseModules.isEmpty()) return 0.0;

        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) return 0.0;

        int totalItems = 0;
        int completedItems = 0;

        for (CourseModules cm : courseModules) {
            com.example.e_learning_system.Entities.Module module = cm.getModule();
            if (module == null) continue;

            // Count videos
            List<ModuleVideos> moduleVideos = moduleVideosRepository
                    .findByModuleAndIsActive(module, true);
            for (ModuleVideos mv : moduleVideos) {
                totalItems++;
                VideoEntity video = mv.getVideo();
                if (video != null && user.getWatchedVideos() != null && 
                    user.getWatchedVideos().contains(video)) {
                    completedItems++;
                }
            }

            // Count quizzes
            List<QuizEntity> quizzes = quizRepository.findAll().stream()
                    .filter(q -> q.getCourse() != null && q.getCourse().getId() == courseId)
                    .filter(QuizEntity::getIsActive)
                    .collect(Collectors.toList());

            for (QuizEntity quiz : quizzes) {
                totalItems++;
                boolean completed = quizSubmissionRepository.findAll().stream()
                        .anyMatch(submission -> 
                                submission.getUser() != null && 
                                submission.getUser().getId() == userId &&
                                submission.getQuiz() != null && 
                                submission.getQuiz().getId() == quiz.getId());
                if (completed) {
                    completedItems++;
                }
            }
        }

        if (totalItems == 0) return 0.0;
        return (completedItems * 100.0) / totalItems;
    }

    private double calcOverallCompletionRate(Integer teacherId) {
        UserEntity teacher = userRepository.findById(teacherId).orElse(null);
        if (teacher == null) return 0.0;

        List<Course> courses = courseRepository.findByCreatedBy(teacher);
        if (courses.isEmpty()) return 0.0;

        int totalEnrollments = 0;
        int totalCompletions = 0;

        for (Course course : courses) {
            List<UserCourseAccess> accesses = userCourseAccessRepository
                    .findByCourseIdAndIsActiveTrue(course.getId());

            for (UserCourseAccess access : accesses) {
                totalEnrollments++;
                double progress = calculateCourseProgressForStudent(access.getUser().getId(), course.getId());
                if (progress >= 100.0) {
                    totalCompletions++;
                }
            }
        }

        if (totalEnrollments == 0) return 0.0;
        return (totalCompletions * 100.0) / totalEnrollments;
    }
}

package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.MyLearningDtos.EnrolledCourseDto;
import com.example.e_learning_system.Dto.MyLearningDtos.MyLearningResponseDto;
import com.example.e_learning_system.Dto.MyLearningDtos.MyLearningStatsDto;
import com.example.e_learning_system.Entities.*;
import com.example.e_learning_system.Repository.*;
import com.example.e_learning_system.Service.Interfaces.MyLearningService;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MyLearningServiceImpl implements MyLearningService {

    private final UserCourseAccessRepository userCourseAccessRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseModulesRepository courseModulesRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final QuizRepository quizRepository;
    private final VideoRepository videoRepository;

    @Value("${app.public-base-url:http://localhost:5000}")
    private String publicBaseUrl;

    @Override
    public MyLearningResponseDto getMyLearningDashboard(Integer userId) {
        log.info("Getting learning dashboard for user {}", userId);

        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFound.userNotFound(userId.toString()));

        MyLearningStatsDto stats = getLearningStats(userId);
        List<EnrolledCourseDto> enrolledCourses = getEnrolledCourses(userId);
        List<EnrolledCourseDto> continueLearning = getContinueLearning(userId, 5);
        List<EnrolledCourseDto> upcomingDeadlines = getUpcomingDeadlines(userId, 30);

        return MyLearningResponseDto.builder()
                .stats(stats)
                .enrolledCourses(enrolledCourses)
                .continueLearning(continueLearning)
                .upcomingDeadlines(upcomingDeadlines)
                .build();
    }

    @Override
    public List<EnrolledCourseDto> getEnrolledCourses(Integer userId) {
        log.info("Getting enrolled courses for user {}", userId);

        List<UserCourseAccess> accesses = userCourseAccessRepository
                .findActiveAccessesByUser(userId, LocalDateTime.now());

        return accesses.stream()
                .map(access -> buildEnrolledCourseDto(access))
                .sorted(Comparator.comparing(EnrolledCourseDto::getEnrolledDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public MyLearningStatsDto getLearningStats(Integer userId) {
        log.info("Getting learning stats for user {}", userId);

        List<UserCourseAccess> allAccesses = userCourseAccessRepository
                .findActiveAccessesByUser(userId, LocalDateTime.now());

        int totalEnrolled = allAccesses.size();
        int activeCourses = 0;
        int completedCourses = 0;
        int totalLessonsCompleted = 0;
        int totalQuizzesCompleted = 0;
        double totalProgress = 0.0;

        // Get user entity to check watched videos
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        for (UserCourseAccess access : allAccesses) {
            Course course = access.getCourse();
            if (course == null) continue;

            // Calculate progress
            double progress = calculateCourseProgress(userId, course.getId());
            totalProgress += progress;

            if (progress >= 100.0) {
                completedCourses++;
            } else if (progress > 0) {
                activeCourses++;
            }

            // Count COMPLETED lessons (watched videos only) - using courseModules
            if (course.getCourseModules() != null) {
                for (CourseModules cm : course.getCourseModules()) {
                    if (cm.getModule() != null && cm.getModule().getVideos() != null) {
                        for (VideoEntity video : cm.getModule().getVideos()) {
                            // Only count videos that the user has actually watched
                            if (user.getWatchedVideos() != null && user.getWatchedVideos().contains(video)) {
                                totalLessonsCompleted++;
                            }
                        }
                    }
                }
            }

            // Count completed quizzes for this course
            List<QuizEntity> quizzes = quizRepository.findAll().stream()
                    .filter(q -> q.getCourse() != null && q.getCourse().getId() == course.getId())
                    .collect(Collectors.toList());
            
            for (QuizEntity quiz : quizzes) {
                List<QuizSubmissionEntity> submissions = quizSubmissionRepository
                        .findByQuizIdAndUserId(quiz.getId(), userId);
                if (!submissions.isEmpty()) {
                    totalQuizzesCompleted++;
                }
            }
        }

        double avgProgress = totalEnrolled > 0 ? totalProgress / totalEnrolled : 0.0;

        return MyLearningStatsDto.builder()
                .totalEnrolledCourses(totalEnrolled)
                .activeCourses(activeCourses)
                .completedCourses(completedCourses)
                .totalLessonsCompleted(totalLessonsCompleted)
                .totalQuizzesCompleted(totalQuizzesCompleted)
                .totalLearningHours(0) // TODO: Calculate from video watch time
                .averageProgressPercentage(avgProgress)
                .certificatesEarned(completedCourses)
                .currentStreak(0) // TODO: Implement streak calculation
                .build();
    }

    @Override
    public List<EnrolledCourseDto> getContinueLearning(Integer userId, int limit) {
        log.info("Getting continue learning courses for user {}", userId);

        List<UserCourseAccess> accesses = userCourseAccessRepository
                .findActiveAccessesByUser(userId, LocalDateTime.now());

        return accesses.stream()
                .filter(access -> {
                    double progress = calculateCourseProgress(userId, access.getCourse().getId());
                    return progress > 0 && progress < 100;
                })
                .sorted(Comparator.comparing(UserCourseAccess::getUpdatedAt).reversed())
                .limit(limit)
                .map(this::buildEnrolledCourseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrolledCourseDto> getUpcomingDeadlines(Integer userId, int daysThreshold) {
        log.info("Getting courses with upcoming deadlines for user {}", userId);

        List<UserCourseAccess> accesses = userCourseAccessRepository
                .findActiveAccessesByUser(userId, LocalDateTime.now());

        LocalDateTime threshold = LocalDateTime.now().plusDays(daysThreshold);

        return accesses.stream()
                .filter(access -> access.getAccessUntil() != null 
                        && access.getAccessUntil().isBefore(threshold))
                .sorted(Comparator.comparing(UserCourseAccess::getAccessUntil))
                .map(this::buildEnrolledCourseDto)
                .collect(Collectors.toList());
    }

    @Override
    public EnrolledCourseDto getEnrolledCourseDetails(Integer userId, Integer courseId) {
        log.info("Getting enrolled course details for user {} and course {}", userId, courseId);

        UserCourseAccess access = userCourseAccessRepository
                .findActiveAccessByUserAndCourse(userId, courseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User does not have active access to this course"));

        return buildEnrolledCourseDto(access);
    }

    /**
     * Build EnrolledCourseDto from UserCourseAccess
     */
    private EnrolledCourseDto buildEnrolledCourseDto(UserCourseAccess access) {
        Course course = access.getCourse();
        Integer userId = access.getUser().getId();
        Integer courseId = course.getId();

        // Get user to check watched videos
        UserEntity user = access.getUser();

        // Get course modules using courseModules relationship and count watched videos
        int totalModules = 0;
        int totalLessons = 0;
        int completedLessons = 0;
        
        if (course.getCourseModules() != null) {
            totalModules = course.getCourseModules().size();
            for (CourseModules cm : course.getCourseModules()) {
                if (cm.getModule() != null && cm.getModule().getVideos() != null) {
                    for (VideoEntity video : cm.getModule().getVideos()) {
                        if (video.getIsActive()) {
                            totalLessons++;
                            // Check if user has watched this video
                            if (user.getWatchedVideos() != null && user.getWatchedVideos().contains(video)) {
                                completedLessons++;
                            }
                        }
                    }
                }
            }
        }

        // Get quizzes for this course
        List<QuizEntity> quizzes = quizRepository.findAll().stream()
                .filter(q -> q.getCourse() != null && q.getCourse().getId() == courseId)
                .collect(Collectors.toList());
        int totalQuizzes = quizzes.size();
        
        // Calculate completed quizzes
        int completedQuizzes = 0;
        for (QuizEntity quiz : quizzes) {
            List<QuizSubmissionEntity> submissions = 
                    quizSubmissionRepository.findByQuizIdAndUserId(quiz.getId(), userId);
            if (!submissions.isEmpty()) {
                completedQuizzes++;
            }
        }

        // Calculate progress
        double progress = calculateCourseProgress(userId, courseId);

        // Get thumbnail URL
        String thumbnailUrl = null;
        if (course.getThumbnail() != null) {
            thumbnailUrl = publicBaseUrl + "/api/attachments/" + course.getThumbnail().getId();
        }

        // Calculate days remaining
        Integer daysRemaining = null;
        boolean hasLifetimeAccess = access.getAccessUntil() == null;
        if (access.getAccessUntil() != null) {
            long days = ChronoUnit.DAYS.between(LocalDateTime.now(), access.getAccessUntil());
            daysRemaining = Math.max(0, (int) days);
        }

        // Get average rating
        Double avgRating = userFeedbackRepository.findAverageRatingByCourseId(courseId);

        // Get instructor name
        String instructorName = course.getCreatedBy() != null 
                ? course.getCreatedBy().getEmail() 
                : "Unknown";

        return EnrolledCourseDto.builder()
                .courseId(courseId)
                .courseName(course.getName())
                .description(course.getDescription())
                .instructor(instructorName)
                .thumbnailUrl(thumbnailUrl)
                .category(course.getCategory())
                .difficultyLevel(course.getDifficultyLevel())
                .status(course.getStatus())
                .totalModules(totalModules)
                .completedModules(0) // TODO: Implement module completion tracking
                .totalLessons(totalLessons)
                .completedLessons(completedLessons) // Now properly counting watched videos
                .totalQuizzes(totalQuizzes)
                .completedQuizzes(completedQuizzes)
                .progressPercentage(progress)
                .totalDurationMinutes(course.getEstimatedDrationInHours() * 60)
                .watchedDurationMinutes(0) // TODO: Implement watch time tracking
                .accessType(access.getAccessType())
                .enrolledDate(access.getCreatedAt())
                .accessUntil(access.getAccessUntil())
                .isActive(access.getIsActive())
                .hasLifetimeAccess(hasLifetimeAccess)
                .daysRemaining(daysRemaining)
                .lastAccessedDate(access.getUpdatedAt())
                .currentModule(null) // TODO: Track current module
                .currentLesson(null) // TODO: Track current lesson
                .averageRating(avgRating)
                .pricePaid(course.getOneTimePrice())
                .packageId(null) // TODO: Add package info if accessed via package
                .packageName(null)
                .build();
    }

    /**
     * Calculate course progress based on quiz completion AND video watch completion
     * Combines both metrics for accurate progress tracking
     */
    private double calculateCourseProgress(Integer userId, Integer courseId) {
        // Get user to check watched videos
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFound.userNotFound(userId.toString()));
        
        double quizProgress = calculateQuizProgress(userId, courseId);
        double videoProgress = calculateVideoProgress(user, courseId);
        
        // If there are both quizzes and videos, average them
        // If only one exists, use that as 100% of progress
        if (quizProgress > 0 && videoProgress > 0) {
            return (quizProgress + videoProgress) / 2.0;
        } else if (quizProgress > 0) {
            return quizProgress;
        } else if (videoProgress > 0) {
            return videoProgress;
        }
        
        return 0.0;
    }
    
    /**
     * Calculate quiz completion progress
     */
    private double calculateQuizProgress(Integer userId, Integer courseId) {
        // Get all quizzes for this course
        List<QuizEntity> quizzes = quizRepository.findAll().stream()
                .filter(q -> q.getCourse() != null && q.getCourse().getId() == courseId)
                .collect(Collectors.toList());
        
        if (quizzes.isEmpty()) {
            return 0.0;
        }

        int totalQuizzes = quizzes.size();
        int completedQuizzes = 0;

        for (QuizEntity quiz : quizzes) {
            List<QuizSubmissionEntity> submissions = 
                    quizSubmissionRepository.findByQuizIdAndUserId(quiz.getId(), userId);
            if (!submissions.isEmpty()) {
                completedQuizzes++;
            }
        }

        return totalQuizzes > 0 ? (completedQuizzes * 100.0) / totalQuizzes : 0.0;
    }
    
    /**
     * Calculate video watch progress
     */
    private double calculateVideoProgress(UserEntity user, Integer courseId) {
        // Get all videos for this course through course modules
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ResourceNotFound.courseNotFound(courseId.toString()));
        
        // Module.getVideos() already returns Set<VideoEntity>
        List<VideoEntity> courseVideos = course.getCourseModules().stream()
                .filter(cm -> cm.getModule() != null)
                .flatMap(cm -> cm.getModule().getVideos().stream())
                .filter(v -> v != null && v.getIsActive())
                .distinct()
                .collect(Collectors.toList());
        
        if (courseVideos.isEmpty()) {
            return 0.0;
        }
        
        // Count how many videos the user has watched
        int totalVideos = courseVideos.size();
        int watchedVideos = 0;
        
        for (VideoEntity video : courseVideos) {
            if (user.getWatchedVideos().contains(video)) {
                watchedVideos++;
            }
        }
        
        return totalVideos > 0 ? (watchedVideos * 100.0) / totalVideos : 0.0;
    }
}

package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.FeedbackDtos.CreateFeedbackDto;
import com.example.e_learning_system.Dto.FeedbackDtos.FeedbackDto;
import com.example.e_learning_system.Entities.UserFeedback;
import com.example.e_learning_system.Repository.UserFeedbackJpaRepository;
import com.example.e_learning_system.Dto.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserFeedbackServiceImpl implements UserFeedbackService {

    private final UserFeedbackJpaRepository feedbackRepo;

    public UserFeedbackServiceImpl(UserFeedbackJpaRepository feedbackRepo) {
        this.feedbackRepo = feedbackRepo;
    }

    @Override
    public PagedResponse<FeedbackDto> getFeedbackForCourse(Integer courseId, int page, int size) {
        PageRequest pr = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<UserFeedback> p = feedbackRepo.findAllByCourseIdAndIsActiveTrueOrderByCreatedAtDesc(courseId, pr);
        java.util.List<FeedbackDto> content = p.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PagedResponse<>(content, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    @Override
    public FeedbackDto createFeedback(Integer courseId, CreateFeedbackDto payload) {
        // basic validation
        if (payload.getRating() != null && (payload.getRating() < 1 || payload.getRating() > 5)) {
            throw new IllegalArgumentException("rating must be 1..5");
        }
        Integer userId = payload.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        // check if a feedback from this user for this course already exists
        Optional<UserFeedback> existing = feedbackRepo.findByCourseIdAndUserIdAndIsActiveTrue(courseId, userId);
        if (existing.isPresent()) {
            UserFeedback f = existing.get();
            f.setFeedbackText(payload.getFeedbackText());
            f.setRating(payload.getRating());
            f.setIsAnonymous(payload.getIsAnonymous() == null ? false : payload.getIsAnonymous());
            f.setUpdatedAt(LocalDateTime.now());
            UserFeedback saved = feedbackRepo.save(f);
            return toDto(saved);
        }

        UserFeedback f = UserFeedback.builder()
                .courseId(courseId)
                .userId(userId)
                .feedbackText(payload.getFeedbackText())
                .rating(payload.getRating())
                .isAnonymous(payload.getIsAnonymous() == null ? false : payload.getIsAnonymous())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserFeedback saved = feedbackRepo.save(f);
        return toDto(saved);
    }

    private FeedbackDto toDto(UserFeedback f) {
        FeedbackDto d = new FeedbackDto();
        d.setId(f.getId());
        d.setUserId(f.getUserId());
        d.setFeedbackText(f.getFeedbackText());
        d.setRating(f.getRating());
        d.setIsAnonymous(f.getIsAnonymous());
        d.setCreatedAt(f.getCreatedAt());
        return d;
    }
}

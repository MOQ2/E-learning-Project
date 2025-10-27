package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.FeedbackDtos.CreateFeedbackDto;
import com.example.e_learning_system.Dto.FeedbackDtos.FeedbackDto;
import com.example.e_learning_system.Dto.PagedResponse;
import com.example.e_learning_system.Service.UserFeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/courses/{courseId}/reviews")
public class FeedbackController {

    private final UserFeedbackService feedbackService;

    public FeedbackController(UserFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<FeedbackDto>> listReviews(@PathVariable Integer courseId,
                                                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                                                  @RequestParam(name = "size", defaultValue = "3") int size) {
        return ResponseEntity.ok(feedbackService.getFeedbackForCourse(courseId, page, size));
    }

    @PostMapping
    public ResponseEntity<FeedbackDto> createReview(@PathVariable Integer courseId, @RequestBody CreateFeedbackDto payload) {
        FeedbackDto created = feedbackService.createFeedback(courseId, payload);
        return ResponseEntity.ok(created);
    }
}

package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.FeedbackDtos.CreateFeedbackDto;
import com.example.e_learning_system.Dto.FeedbackDtos.FeedbackDto;
import com.example.e_learning_system.Dto.PagedResponse;

public interface UserFeedbackService {
    PagedResponse<FeedbackDto> getFeedbackForCourse(Integer courseId, int page, int size);
    FeedbackDto createFeedback(Integer courseId, CreateFeedbackDto payload);
}

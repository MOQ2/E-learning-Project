package com.example.e_learning_system.Dto.FeedbackDtos;

import lombok.Data;

@Data
public class CreateFeedbackDto {
    private Integer userId;
    private String feedbackText;
    private Integer rating; // 1..5
    private Boolean isAnonymous = false;
}

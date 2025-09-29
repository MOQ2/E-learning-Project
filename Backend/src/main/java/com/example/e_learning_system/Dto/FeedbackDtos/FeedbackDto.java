package com.example.e_learning_system.Dto.FeedbackDtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackDto {
    private Integer id;
    private Integer userId;
    private String feedbackText;
    private Integer rating;
    private Boolean isAnonymous;
    private LocalDateTime createdAt;
}

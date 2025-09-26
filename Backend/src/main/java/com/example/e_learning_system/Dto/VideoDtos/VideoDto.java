package com.example.e_learning_system.Dto.VideoDtos;


import com.example.e_learning_system.Dto.AttachmentDtos.AttachmentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    private Integer id;
    private String title;
    private Map<String, Object> metadata;
    private AttachmentDto thumbnail;
    private Integer durationSeconds;
    private Boolean isActive;
    private Integer uploadedById;
    private String uploadedByName;
    private List<AttachmentDto> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String explanation;
    private String whatWeWillLearn;
    private String status;
    private String prerequisites;
}
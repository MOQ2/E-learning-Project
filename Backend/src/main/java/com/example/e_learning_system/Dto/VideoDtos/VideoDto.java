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
    private String videoKey;
    private String thumbnailUrl;
    private Integer durationSeconds;
    private Boolean isActive;
    private Integer uploadedById;
    private String uploadedByName;
    private List<AttachmentDto> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
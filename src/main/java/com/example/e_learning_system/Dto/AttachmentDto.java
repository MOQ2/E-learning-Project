package com.example.e_learning_system.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {
    private Integer id;
    private String title;
    private Map<String, Object> metadata;
    private String url;
    private long size;
    private String fileType;
    private boolean isActive;
    private Integer uploadedById;
    private String uploadedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

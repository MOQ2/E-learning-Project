package com.example.e_learning_system.Dto.AttachmentDtos;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentSummaryDto {
    private int id;
    private String title;
    private Map<String,Object> metadata;
    private boolean isActive;
    private int uploadedByUserId;
}

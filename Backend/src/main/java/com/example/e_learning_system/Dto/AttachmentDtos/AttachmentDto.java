package com.example.e_learning_system.Dto.AttachmentDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AttachmentDto extends AttachmentSummaryDto {
    private String fileDownloadUrl;
    private String fileName;
    private String contentType;
    private long fileSize;
}

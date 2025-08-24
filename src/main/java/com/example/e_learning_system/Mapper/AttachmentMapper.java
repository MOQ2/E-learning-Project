package com.example.e_learning_system.Mapper;

import com.example.e_learning_system.Dto.AttachmentDtos.AttachmentDto;
import com.example.e_learning_system.Dto.AttachmentDtos.AttachmentSummaryDto;
import com.example.e_learning_system.Dto.AttachmentDtos.CreateAttachmentDto;
import com.example.e_learning_system.Dto.AttachmentDtos.UpdateAttachmentDto;
import com.example.e_learning_system.Entities.Attachment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttachmentMapper {

    private AttachmentMapper() {
        // private constructor to prevent instantiation
    }

    // ====== ENTITY -> DTO ======
    public static AttachmentDto fromEntity(Attachment attachment) {
        if (attachment == null) return null;

        AttachmentDto dto = AttachmentDto.builder()
                .fileDownloadUrl("/api/attachments/" + attachment.getId() + "/download")
                .fileName(getMetadataValue(attachment.getMetadata(), "fileName", String.class))
                .contentType(getMetadataValue(attachment.getMetadata(), "contentType", String.class))
                .fileSize(attachment.getSize())
                .build();

        // Inherit fields from AttachmentSummaryDto
        dto.setId(attachment.getId());
        dto.setTitle(attachment.getTitle());
        dto.setMetadata(attachment.getMetadata());
        dto.setActive(attachment.isActive());
        dto.setUploadedByUserId(attachment.getUploadedBy() != null ?
                attachment.getUploadedBy().getId() : -1);

        return dto;
    }

    public static AttachmentSummaryDto fromEntityToSummary(Attachment attachment) {
        if (attachment == null) return null;

        AttachmentSummaryDto dto = new AttachmentSummaryDto();
        dto.setId(attachment.getId());
        dto.setTitle(attachment.getTitle());
        dto.setMetadata(attachment.getMetadata());
        dto.setActive(attachment.isActive());
        dto.setUploadedByUserId(attachment.getUploadedBy() != null ?
                attachment.getUploadedBy().getId() : -1);

        return dto;
    }

    // ====== DTO -> ENTITY ======
    public static Attachment toEntity(CreateAttachmentDto dto) {
        if (dto == null) return null;

        Attachment attachment = new Attachment();
        attachment.setTitle(dto.getTitle());
        attachment.setActive(true);
        attachment.setMetadata(new HashMap<>());

        return attachment;
    }

    public static void updateEntity(UpdateAttachmentDto dto, Attachment existingAttachment) {
        if (dto == null || existingAttachment == null) return;

        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            existingAttachment.setTitle(dto.getTitle());
        }
    }

    // ====== HELPER METHODS ======
    public static void setFileMetadata(Attachment attachment, String fileName, String contentType, long size) {
        Map<String, Object> metadata = attachment.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
            attachment.setMetadata(metadata);
        }

        metadata.put("fileName", fileName);
        metadata.put("contentType", contentType);
        metadata.put("size", size);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getMetadataValue(Map<String, Object> metadata, String key, Class<T> type) {
        if (metadata == null) return null;
        Object value = metadata.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }

    public static List<AttachmentDto> fromEntitiesToAttachmentDto(List<Attachment> attachments) {
        return attachments == null ? null :
                attachments.stream()
                        .map(AttachmentMapper::fromEntity)
                        .collect(Collectors.toList());
    }

    public static List<AttachmentSummaryDto> fromEntitiesToSummaries(List<Attachment> attachments) {
        return attachments == null ? null :
                attachments.stream()
                        .map(AttachmentMapper::fromEntityToSummary)
                        .collect(Collectors.toList());
    }
}

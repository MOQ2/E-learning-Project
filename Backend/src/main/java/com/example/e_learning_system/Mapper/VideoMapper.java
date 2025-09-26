package com.example.e_learning_system.Mapper;

import com.example.e_learning_system.Dto.AttachmentDtos.AttachmentDto;
import com.example.e_learning_system.Dto.VideoDtos.CreatVideoDto;
import com.example.e_learning_system.Dto.VideoDtos.VideoDto;
import com.example.e_learning_system.Entities.Attachment;
import com.example.e_learning_system.Entities.VideoEntity;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Entities.VideoAttachments;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class VideoMapper {

    /**
     * Maps from VideoEntity to VideoDto
     */
    public static VideoDto fromVideoEntityToVideoDto(VideoEntity videoEntity) {
        if (videoEntity == null) {
            return null;
        }

        return VideoDto.builder()
                .id(videoEntity.getId())
                .title(videoEntity.getTitle())
                .metadata(videoEntity.getMetadata())
                .thumbnail(AttachmentMapper.fromEntity(videoEntity.getThumbnail()))
                .durationSeconds(videoEntity.getDurationSeconds())
                .isActive(videoEntity.getIsActive())
                .uploadedById(videoEntity.getUploadedBy() != null ? videoEntity.getUploadedBy().getId() : null)
                .uploadedByName(videoEntity.getUploadedBy() != null ? videoEntity.getUploadedBy().getName()  : null)
                .attachments(fromVideoAttachmentsToAttachmentDtos(videoEntity.getVideoAttachments()))
                .createdAt(videoEntity.getCreatedAt())
                .updatedAt(videoEntity.getUpdatedAt())
                .explanation(videoEntity.getExplanation())
                .whatWeWillLearn(videoEntity.getWhatWeWillLearn())
                .status(videoEntity.getStatus())
                .prerequisites(videoEntity.getPrerequisites())
                .build();
    }

    /**
     * Maps from CreatVideoDto to VideoEntity
     */
    public static VideoEntity fromCreatVideoDtoToVideoEntity(CreatVideoDto createVideoDto, UserEntity uploadedBy) {
        if (createVideoDto == null) {
            return null;
        }

        VideoEntity videoEntity = new VideoEntity();
        videoEntity.setTitle(createVideoDto.getTitle());
        videoEntity.setDurationSeconds(createVideoDto.getDurationSeconds());
        videoEntity.setUploadedBy(uploadedBy);
        videoEntity.setIsActive(true);
        videoEntity.setMetadata(Collections.emptyMap()); // Initialize with empty map
        videoEntity.setCreatedAt(LocalDateTime.now());
        videoEntity.setUpdatedAt(LocalDateTime.now());
        videoEntity.setExplanation(createVideoDto.getExplanation());
        videoEntity.setWhatWeWillLearn(createVideoDto.getWhatWeWillLearn());
        videoEntity.setStatus(createVideoDto.getStatus());
        videoEntity.setPrerequisites(createVideoDto.getPrerequisites());

        return videoEntity;
    }

    /**
     * Maps a list of VideoEntity to a list of VideoDto
     */
    public static List<VideoDto> fromVideoEntitiesToVideoDtos(List<VideoEntity> videoEntities) {
        if (videoEntities == null) {
            return Collections.emptyList();
        }

        return videoEntities.stream()
                .map(VideoMapper::fromVideoEntityToVideoDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing VideoEntity with data from CreatVideoDto
     */
    public static void updateVideoEntityFromCreatVideoDto(VideoEntity existingVideoEntity, CreatVideoDto createVideoDto) {
        if (existingVideoEntity == null || createVideoDto == null) {
            return;
        }

        existingVideoEntity.setTitle(createVideoDto.getTitle());
        existingVideoEntity.setDurationSeconds(createVideoDto.getDurationSeconds());
        existingVideoEntity.setUpdatedAt(LocalDateTime.now());
        existingVideoEntity.setExplanation(createVideoDto.getExplanation());
        existingVideoEntity.setWhatWeWillLearn(createVideoDto.getWhatWeWillLearn());
        existingVideoEntity.setStatus(createVideoDto.getStatus());
        existingVideoEntity.setPrerequisites(createVideoDto.getPrerequisites());
    }

    /**
     * Helper method to map VideoAttachments to AttachmentDtos
     */
    private static List<AttachmentDto> fromVideoAttachmentsToAttachmentDtos(Set<VideoAttachments> videoAttachments) {
        if (videoAttachments == null || videoAttachments.isEmpty()) {
            return Collections.emptyList();
        }
        return videoAttachments.stream()
                .map(videoAttachment -> AttachmentMapper.fromEntity(videoAttachment.getAttachment()))
                .filter(Objects::nonNull) // Filter out nulls if needed
                .collect(Collectors.toList());
    }

    /**
     * Maps from VideoDto to VideoEntity (for update operations)
     */
    public static VideoEntity fromVideoDtoToVideoEntity(VideoDto videoDto) {
        if (videoDto == null) {
            return null;
        }

        VideoEntity videoEntity = new VideoEntity();
        videoEntity.setId(videoDto.getId());
        videoEntity.setTitle(videoDto.getTitle());
        videoEntity.setMetadata(videoDto.getMetadata());
        if (videoDto.getThumbnail() != null) {
            Attachment thumbnail = new Attachment();
            thumbnail.setId(videoDto.getThumbnail().getId());
            videoEntity.setThumbnail(thumbnail);
        }
        videoEntity.setDurationSeconds(videoDto.getDurationSeconds());
        videoEntity.setIsActive(videoDto.getIsActive());
        videoEntity.setExplanation(videoDto.getExplanation());
        videoEntity.setWhatWeWillLearn(videoDto.getWhatWeWillLearn());
        videoEntity.setStatus(videoDto.getStatus());
        videoEntity.setPrerequisites(videoDto.getPrerequisites());
        videoEntity.setCreatedAt(videoDto.getCreatedAt());
        videoEntity.setUpdatedAt(videoDto.getUpdatedAt());

        return videoEntity;
    }
}
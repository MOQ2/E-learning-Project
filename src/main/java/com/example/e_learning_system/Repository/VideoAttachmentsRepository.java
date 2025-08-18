package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.Attachment;
import com.example.e_learning_system.Entities.VideoAttachments;
import com.example.e_learning_system.Entities.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoAttachmentsRepository extends JpaRepository<VideoAttachments, Integer> {

    // Find by video
    List<VideoAttachments> findByVideo(VideoEntity video);

    // Find by attachment
    List<VideoAttachments> findByAttachment(Attachment attachment);

    // Find by video and active status
    List<VideoAttachments> findByVideoAndIsActive(VideoEntity video, boolean isActive);

    // Find by attachment and active status
    List<VideoAttachments> findByAttachmentAndIsActive(Attachment attachment, boolean isActive);

    // Find by active status
    List<VideoAttachments> findByIsActive(boolean isActive);

    // Find by video and attachment
    Optional<VideoAttachments> findByVideoAndAttachment(VideoEntity video, Attachment attachment);

    // Check if video-attachment relationship exists
    boolean existsByVideoAndAttachment(VideoEntity video, Attachment attachment);
}

package com.example.e_learning_system.Entities;

import com.example.e_learning_system.Entities.BaseEntity;
import com.example.e_learning_system.Entities.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "videos")
public class VideoEntity extends BaseEntity {

    @Column(name = "title", length = 250)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metadata;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoKey;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Override
    @Transient
    public String getEntityType() {
        return "VideoEntity";
    }

    // relations
    // done
    // uploded by

    // not done
    // attachemnt need to create seperate table for the many to many relation

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private UserEntity uploadedBy;


    @OneToMany(
            mappedBy = "video",
            fetch = FetchType.LAZY
    )
    @OrderBy("videoOrder ASC")
    private List<ModuleVideos> moduleVideos = new ArrayList<>();

    @OneToMany(
            mappedBy = "video",
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.ALL
    )
    private Set <VideoAttachments> videoAttachments = new HashSet<>();



    public void addVideoAttachments(VideoAttachments videoAttachments) {
        this.videoAttachments.add(videoAttachments);
    }
    public void removeVideoAttachments(VideoAttachments videoAttachments) {
        this.videoAttachments.remove(videoAttachments);
    }
    public void removeVideoAttachmentByids(int videoId , int attachmentid){
        for (VideoAttachments videoAttachment : this.videoAttachments) {
            if (videoAttachment.getAttachment().getId() == videoId && videoAttachment.getAttachment().getId() == attachmentid) {
                this.videoAttachments.remove(videoAttachment);
                return;
            }
        }
    }




}
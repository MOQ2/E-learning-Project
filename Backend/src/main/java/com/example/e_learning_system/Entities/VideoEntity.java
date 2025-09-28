package com.example.e_learning_system.Entities;

import jakarta.persistence.*;
import lombok.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thumbnail_id")
    private Attachment thumbnail;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "what_we_will_learn", columnDefinition = "TEXT")
    private String whatWeWillLearn;

    @Column(name = "status", columnDefinition = "TEXT")
    private String status;

    @Column(name = "prerequisites", columnDefinition = "TEXT")
    private String prerequisites;

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

    @ManyToMany(mappedBy = "watchedVideos")
    private Set<UserEntity> usersWatched = new HashSet<>();




    public void addVideoAttachments(VideoAttachments videoAttachments) {
        this.videoAttachments.add(videoAttachments);
    }
    public void removeVideoAttachments(VideoAttachments videoAttachments) {
        this.videoAttachments.remove(videoAttachments);
    }
    public void removeVideoAttachmentByids(int videoId , int attachmentid){
        VideoAttachments toRemove = null;
        for (VideoAttachments videoAttachment : this.videoAttachments) {
            if (videoAttachment.getVideo().getId() == videoId && videoAttachment.getAttachment().getId() == attachmentid) {
                toRemove = videoAttachment;
                break;
            }
        }
        if (toRemove != null) {
            this.videoAttachments.remove(toRemove);
        }
    }


    public void setAttachments(List<Attachment> attachments) {
        this.videoAttachments.clear();
        if (attachments != null) {
            for (Attachment attachment : attachments) {
                this.videoAttachments.add(new VideoAttachments(this, attachment, isActive));
            }
        }
    }


}
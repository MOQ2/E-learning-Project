package com.example.e_learning_system.Entities;

import com.example.e_learning_system.Entities.BaseEntity;
import com.example.e_learning_system.Entities.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private UserEntity uploadedBy;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Override
    @Transient
    public String getEntityType() {
        return "VideoEntity";
    }
}
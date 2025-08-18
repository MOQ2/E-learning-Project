package com.example.e_learning_system.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "module_videos", uniqueConstraints = {

        @UniqueConstraint(
                name = "unique_module_video_order",
                columnNames = {"module_id", "video_order"}
        )
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ModuleVideos extends BaseEntity {

    @Column(name = "video_order", nullable = false)
    private Integer videoOrder;


    @Column(name = "is_active")
    private boolean isActive = true;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private VideoEntity video;

}
package com.example.e_learning_system.Entities;

import com.example.e_learning_system.AuditListener.AuditListener;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.*;

@Getter
@Setter
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, AuditListener.class})
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private Map<String, Object> oldState;

    @PostLoad
    public void postLoad() {
        this.oldState = AuditListener.extractData(this);
    }
}

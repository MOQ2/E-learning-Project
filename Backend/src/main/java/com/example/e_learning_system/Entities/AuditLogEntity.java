package com.example.e_learning_system.Entities;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLogEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "entity_type", length = 50, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", length = 10, nullable = false)
    private String action;

    @Type(JsonType.class)
    @Column(name = "changes", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> changes;
}

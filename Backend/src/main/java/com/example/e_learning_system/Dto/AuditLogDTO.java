package com.example.e_learning_system.Dto;

import java.util.Map;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuditLogDTO {
    private Long entityId;
    private String entityType;
    private Map<String, Object> changes;
}

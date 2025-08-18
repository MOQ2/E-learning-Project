package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Entities.BaseEntity;

import java.util.Map;

public interface AuditLogInterface {
    void logChange(Long userId, BaseEntity entity, String action,
                   Map<String, Object> oldData, Map<String, Object> newData);
}

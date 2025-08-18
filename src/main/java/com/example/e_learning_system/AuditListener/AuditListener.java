package com.example.e_learning_system.AuditListener;

import com.example.e_learning_system.Entities.AuditLogEntity;
import com.example.e_learning_system.Entities.BaseEntity;
import com.example.e_learning_system.Security.CustomUserDetails;
import com.example.e_learning_system.Service.Interfaces.AuditLogInterface;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuditListener {

    private static AuditLogInterface auditLogInterface;

    @Autowired
    public void init(AuditLogInterface auditLogInterface) {
        AuditListener.auditLogInterface = auditLogInterface;
    }

    @PostPersist
    public void postPersist(Object entity) {
        if (entity instanceof AuditLogEntity) return;

        Map<String, Object> newData = extractData(entity);
        Long userId = getUserId();

        if (userId == null) userId = 2L;

        auditLogInterface.logChange(userId, (BaseEntity) entity, "CREATE",
                Collections.emptyMap(), newData);
    }

    @PostUpdate
    public void postUpdate(Object entity) {
        if (!(entity instanceof BaseEntity base)) return;
        if (entity instanceof AuditLogEntity) return;

        Map<String, Object> oldData = base.getOldState();
        Map<String, Object> newData = extractData(entity);

        Long userId = getUserId();
        if (userId == null) return;

        auditLogInterface.logChange(userId, base, "UPDATE", oldData, newData);
        base.setOldState(null);
    }

    @PostRemove
    public void postRemove(Object entity) {
        if (entity instanceof AuditLogEntity) return;

        Map<String, Object> oldData = extractData(entity);
        Long userId = getUserId();
        if (userId == null) return;

        auditLogInterface.logChange(userId, (BaseEntity) entity, "DELETE",
                oldData, Collections.emptyMap());
    }

    private Long getUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) return null;

            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                return userDetails.getId();
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }



    public static Map<String, Object> extractData(Object entity) {
        Map<String, Object> map = new HashMap<>();
        if (entity == null) return map;

        Class<?> clazz = entity.getClass();
        map.put("entity", clazz.getSimpleName());

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);

                if (value == null) continue;

                if (value instanceof java.util.Collection) continue;
                if (value.getClass().isArray()) {
                    value = java.util.Arrays.asList((Object[]) value);
                }

                map.put(field.getName(), value);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

}
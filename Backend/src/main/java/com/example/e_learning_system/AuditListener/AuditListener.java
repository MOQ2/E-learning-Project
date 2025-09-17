package com.example.e_learning_system.AuditListener;

import com.example.e_learning_system.Entities.AuditLogEntity;
import com.example.e_learning_system.Entities.BaseEntity;
import com.example.e_learning_system.Security.UserUtil;
import com.example.e_learning_system.Service.Interfaces.AuditLog;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuditListener {

    private static AuditLog auditLogInterface;

    @Autowired
    public void init(AuditLog auditLogInterface) {
        AuditListener.auditLogInterface = auditLogInterface;
    }

    @PostPersist
    public void postPersist(BaseEntity entity) {
        if (entity instanceof AuditLogEntity) return;

        Map<String, Object> newData = extractData(entity);
        Long userId = UserUtil.getCurrentUserId();
        if (userId == null) userId = 2L;

        auditLogInterface.logChange(userId, entity, "CREATE",
                Collections.emptyMap(), newData);
    }

    @PostUpdate
    public void postUpdate(BaseEntity entity) {
        if (entity instanceof AuditLogEntity) return;

        Map<String, Object> oldData = entity.getOldState();
        Map<String, Object> newData = extractData(entity);

        Long userId = UserUtil.getCurrentUserId();
        if (userId == null) return;

        auditLogInterface.logChange(userId, entity, "UPDATE", oldData, newData);
        entity.setOldState(null);
    }

    @PostRemove
    public void postRemove(BaseEntity entity) {
        if (entity instanceof AuditLogEntity) return;

        Map<String, Object> oldData = extractData(entity);
        Long userId = UserUtil.getCurrentUserId();
        if (userId == null) return;

        auditLogInterface.logChange(userId, entity, "DELETE",
                oldData, Collections.emptyMap());
    }




    public static Map<String, Object> extractData(BaseEntity entity) {
        Map<String, Object> map = new HashMap<>();
        if (entity == null) return map;

        map.put("entity", entity.getEntityType());

        Field[] fields = entity.getClass().getDeclaredFields();
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
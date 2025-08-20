package com.example.e_learning_system.Mapper;

import com.example.e_learning_system.Dto.AuditLogDTO;
import com.example.e_learning_system.Entities.BaseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class AuditMapper {

    public AuditLogDTO EntityToDto(BaseEntity entity, Map<String, Object> oldData,
                                   Map<String, Object> newData, String action) {

        Map<String, Object> changes = new HashMap<>();

        Map<String, Object> oldDataSafe = oldData != null ? oldData : Map.of();
        Map<String, Object> newDataSafe = newData != null ? newData : Map.of();

        switch (action) {
            case "UPDATE":
                for (String key : newDataSafe.keySet()) {
                    Object oldValue = oldDataSafe.get(key);
                    Object newValue = newDataSafe.get(key);
                    if (!Objects.equals(oldValue, newValue)) {
                        changes.put(key, Arrays.asList(serializeValue(oldValue), serializeValue(newValue)));
                    }
                }
                break;

            case "CREATE":
                newDataSafe.forEach((k, v) -> changes.put(k, Arrays.asList(null, serializeValue(v))));
                break;

            case "DELETE":
                oldDataSafe.forEach((k, v) -> changes.put(k, Arrays.asList(serializeValue(v), null)));
                break;
        }

        return new AuditLogDTO(
                (long) entity.getId(),
                entity.getClass().getSimpleName(),
                changes
        );
    }


    private Object serializeValue(Object value) {
        if (value == null) return null;

        if (value instanceof BaseEntity base) {
            return base.getId();
        }

        if (value instanceof Enum<?>) {
            return value.toString();
        }

        return value;
    }
}

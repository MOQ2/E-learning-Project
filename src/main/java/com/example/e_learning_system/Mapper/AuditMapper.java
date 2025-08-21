package com.example.e_learning_system.Mapper;

import com.example.e_learning_system.Dto.AuditLogDTO;
import com.example.e_learning_system.Entities.BaseEntity;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AuditMapper {

    public AuditLogDTO EntityToDto(BaseEntity entity, Map<String, Object> oldData,
                                   Map<String, Object> newData, String action) {

        Map<String, Object> changes = new HashMap<>();

        Map<String, Object> oldDataSafe = oldData != null ? oldData : new HashMap<>();
        Map<String, Object> newDataSafe = newData != null ? newData : new HashMap<>();

        switch (action) {
            case "UPDATE":
                for (String key : newDataSafe.keySet()) {
                    Object oldValue = oldDataSafe.get(key);
                    Object newValue = newDataSafe.get(key);
                    if (!areEqual(oldValue, newValue)) {
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

    private boolean areEqual(Object oldValue, Object newValue) {
        if (oldValue instanceof Collection<?> oldCol && newValue instanceof Collection<?> newCol) {
            return compareCollection(oldCol, newCol);
        }
        if (oldValue != null && oldValue.getClass().isArray()
                && newValue != null && newValue.getClass().isArray()) {
            return compareCollection(Arrays.asList((Object[]) oldValue),
                    Arrays.asList((Object[]) newValue));
        }
        return Objects.equals(oldValue, newValue);
    }

    private boolean compareCollection(Collection<?> c1, Collection<?> c2) {
        if (c1.size() != c2.size()) return false;

        var list1 = c1.stream().map(this::serializeValue).sorted().toList();
        var list2 = c2.stream().map(this::serializeValue).sorted().toList();

        return list1.equals(list2);
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

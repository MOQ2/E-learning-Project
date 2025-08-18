package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.AuditLogDTO;
import com.example.e_learning_system.Entities.AuditLogEntity;
import com.example.e_learning_system.Entities.BaseEntity;
import com.example.e_learning_system.Mapper.AuditMapper;
import com.example.e_learning_system.Repository.AuditLogRepository;
import com.example.e_learning_system.Service.Interfaces.AuditLogInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService implements AuditLogInterface {

    private final AuditLogRepository auditLogRepository;
    private final AuditMapper auditMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logChange(Long userId, BaseEntity entity, String action,
                          Map<String, Object> oldData,
                          Map<String, Object> newData) {


        AuditLogDTO dto = auditMapper.EntityToDto(entity, oldData, newData, action);

        AuditLogEntity auditLogEntity = new AuditLogEntity();
        auditLogEntity.setEntityType(dto.getEntityType());
        auditLogEntity.setEntityId(dto.getEntityId());
        auditLogEntity.setUserId(userId);
        auditLogEntity.setAction(action);
        auditLogEntity.setChanges(dto.getChanges());

        auditLogRepository.save(auditLogEntity);
    }
}

package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity,Integer > {

}


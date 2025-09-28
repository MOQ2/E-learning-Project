package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.Module;
import com.example.e_learning_system.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer> {


    // Find by active status
    List<Module> findByIsActive(boolean isActive);

    // Find by creator
    List<Module> findByCreatedBy(UserEntity createdBy);

    // Find by name containing (case insensitive)
    List<Module> findByNameContainingIgnoreCase(String name);

    // Find modules by estimated duration range
    List<Module> findByEstimatedDurationBetween(int minDuration, int maxDuration);

    // Find modules by creator and active status
    List<Module> findByCreatedByAndIsActive(UserEntity createdBy, boolean isActive);
}
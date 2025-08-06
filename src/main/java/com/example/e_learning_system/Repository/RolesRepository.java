package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Config.RolesName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolesRepository extends JpaRepository<RolesEntity, Long> {
    Optional<RolesEntity> findByName(RolesName name);
}

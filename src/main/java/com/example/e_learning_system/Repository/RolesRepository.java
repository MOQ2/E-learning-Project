package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Config.RolesName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<RolesEntity, Integer> {
    Optional<RolesEntity> findByName(RolesName name);
    Optional<RolesEntity> findById(int id);

    boolean existsByName(RolesName name);
    boolean existsById(int id);
    boolean existsByNameAndId(RolesName name, int id);

}

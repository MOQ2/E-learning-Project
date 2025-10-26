package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.PermissionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionsEntityRepository extends JpaRepository<PermissionsEntity, Integer> {

    public Optional<PermissionsEntity> findById(int id);
    public List<PermissionsEntity> findAll();
}

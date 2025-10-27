package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Config.RolesName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<RolesEntity, Integer> {
    public Optional<RolesEntity> findByName(RolesName name);
    public Optional<RolesEntity> findById(int id);
    public List<RolesEntity> findAll();
    public boolean existsByName(RolesName name);
    public boolean existsById(int id);
    public boolean existsByNameAndId(RolesName name, int id);
    public void deleteById(int id);

}

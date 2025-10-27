package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<VideoEntity,Integer> {
    Optional<VideoEntity> findById(int id);
    void deleteById(int id);
    void deleteByVideoKey(String videoKey);
    Optional<VideoEntity> findByVideoKey(String videoKey);

}

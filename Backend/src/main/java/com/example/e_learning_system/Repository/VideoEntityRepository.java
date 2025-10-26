package com.example.e_learning_system.Repository;


import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Entities.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoEntityRepository extends JpaRepository<VideoEntity, Integer> {

    // Find by active status
    List<VideoEntity> findByIsActive(boolean isActive);

    // Find by uploaded user
    List<VideoEntity> findByUploadedBy(UserEntity uploadedBy);

    // Find active videos by user
    List<VideoEntity> findByUploadedByAndIsActive(UserEntity uploadedBy, boolean isActive);

    // Find by title containing (case insensitive)
    List<VideoEntity> findByTitleContainingIgnoreCase(String title);

    // Find by duration range
    List<VideoEntity> findByDurationSecondsBetween(Integer minDuration, Integer maxDuration);



    // Custom query to find videos by metadata key
    @Query("SELECT v FROM VideoEntity v WHERE JSON_EXTRACT(v.metadata, :key) IS NOT NULL")
    List<VideoEntity> findByMetadataKey(@Param("key") String key);

    // Find by video key
    List<VideoEntity> findByVideoKey(String videoKey);
}
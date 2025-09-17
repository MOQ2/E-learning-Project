package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.Module;
import com.example.e_learning_system.Entities.ModuleVideos;
import com.example.e_learning_system.Entities.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleVideosRepository extends JpaRepository<ModuleVideos, Integer> {

    // Find by module
    List<ModuleVideos> findByModule(Module module);

    // Find by video
    List<ModuleVideos> findByVideo(VideoEntity video);

    // Find by module and active status
    List<ModuleVideos> findByModuleAndIsActive(Module module, boolean isActive);

    // Find by module ordered by video order
    List<ModuleVideos> findByModuleOrderByVideoOrderAsc(Module module);

    // Find active videos for a module ordered by video order
    List<ModuleVideos> findByModuleAndIsActiveOrderByVideoOrderAsc(Module module, boolean isActive);

    // Find by module and video order
    Optional<ModuleVideos> findByModuleAndVideoOrder(Module module, Integer videoOrder);

    // Check if video order exists for a module
    boolean existsByModuleAndVideoOrder(Module module, Integer videoOrder);

    // Get maximum video order for a module
    @Query("SELECT MAX(mv.videoOrder) FROM ModuleVideos mv WHERE mv.module = :module")
    Optional<Integer> findMaxVideoOrderByModule(@Param("module") Module module);

    // Find by active status
    List<ModuleVideos> findByIsActive(boolean isActive);

    // Find by module and video
    Optional<ModuleVideos> findByModuleAndVideo(Module module, VideoEntity video);
}
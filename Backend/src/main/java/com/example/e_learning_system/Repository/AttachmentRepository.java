package com.example.e_learning_system.Repository;


import com.example.e_learning_system.Entities.Attachment;
import com.example.e_learning_system.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

    Optional<Attachment> findById(int id);

    // Find by active status
    List<Attachment> findByIsActive(boolean isActive);

    @Query(
            value = "SELECT * FROM attachments WHERE metadata ->> 'fileType' = :fileType",
            nativeQuery = true
    )
    List<Attachment> findByFileType(@Param("fileType") String fileType);

    // Find by uploaded user
    List<Attachment> findByUploadedBy(UserEntity uploadedBy);

    // Find active attachments by user
    List<Attachment> findByUploadedByAndIsActive(UserEntity uploadedBy, boolean isActive);

    // Find by title containing (case insensitive)
    List<Attachment> findByTitleContainingIgnoreCase(String title);

    @Query(
            value = "SELECT * FROM attachments WHERE CAST(metadata ->> 'size' AS bigint) BETWEEN :minSize AND :maxSize",
            nativeQuery = true
    )
    List<Attachment> findBySizeBetween(@Param("minSize") long minSize, @Param("maxSize") long maxSize);

    // Custom query to find attachments by metadata key
    @Query("SELECT a FROM Attachment a WHERE JSON_EXTRACT(a.metadata, :key) IS NOT NULL")
    List<Attachment> findByMetadataKey(@Param("key") String key);
}
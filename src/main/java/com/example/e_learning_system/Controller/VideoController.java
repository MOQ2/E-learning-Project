package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Entities.VideoEntity;
import com.example.e_learning_system.Service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    public VideoController(S3Service s3Service, ObjectMapper objectMapper) {
        this.s3Service = s3Service;
        this.objectMapper = objectMapper;
    }

    // Endpoint for uploading a video with complete metadata
    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("uploadedBy") Integer uploadedByUserId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "metadata", required = false) String additionalMetadataJson) {

        try {
            // Validate required fields
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File cannot be empty"));
            }

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Title is required"));
            }

            if (uploadedByUserId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "uploadedBy user ID is required"));
            }

            // Validate file type (optional - add your allowed types)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only video files are allowed"));
            }

            // Parse additional metadata if provided
            Map<String, Object> additionalMetadata = new HashMap<>();
            if (category != null && !category.trim().isEmpty()) {
                additionalMetadata.put("category", category.trim());
            }

            if (tags != null && !tags.trim().isEmpty()) {
                // Split tags by comma and clean them
                String[] tagArray = tags.split(",");
                List<String> cleanTags = List.of(tagArray).stream()
                        .map(String::trim)
                        .filter(tag -> !tag.isEmpty())
                        .toList();
                additionalMetadata.put("tags", cleanTags);
            }

            // Parse additional JSON metadata if provided
            if (additionalMetadataJson != null && !additionalMetadataJson.trim().isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonMetadata = objectMapper.readValue(additionalMetadataJson, Map.class);
                    additionalMetadata.putAll(jsonMetadata);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid JSON in metadata parameter"));
                }
            }

            // Upload video
            VideoEntity videoEntity = s3Service.uploadVideo(file, title.trim(), uploadedByUserId,
                    description, additionalMetadata);

            // Return success response with video details
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Video uploaded successfully");
            response.put("videoId", videoEntity.getId());
            response.put("videoKey", videoEntity.getVideoKey());
            response.put("title", videoEntity.getTitle());
            response.put("videoUrl", videoEntity.getVideoKey());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    // Endpoint for getting a secure viewing URL
    @GetMapping("/{videoKey}/url")
    public ResponseEntity<?> getVideoUrl(@PathVariable String videoKey,
                                         @RequestParam(value = "duration", defaultValue = "5") Integer durationMinutes) {
        try {
            // Check if video exists
            Optional<VideoEntity> videoOpt = s3Service.getVideoByKey(videoKey);
            if (videoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            VideoEntity video = videoOpt.get();

            // Check if video is active
            if (!video.getIsActive()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Video is not active"));
            }

            // TODO: Add permission check here before generating URL
            // Example: checkUserPermission(currentUserId, video.getUploadedBy().getId());

            // Validate duration (max 60 minutes for security)
            if (durationMinutes > 60) {
                durationMinutes = 60;
            }

            String presignedUrl = s3Service.generatePresignedUrl(videoKey, Duration.ofMinutes(durationMinutes));

            Map<String, Object> response = new HashMap<>();
            response.put("videoKey", videoKey);
            response.put("title", video.getTitle());
            response.put("presignedUrl", presignedUrl);
            response.put("expiresInMinutes", durationMinutes);
            response.put("duration", video.getDurationSeconds());
            response.put("thumbnailUrl", video.getThumbnailUrl());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to generate URL: " + e.getMessage()));
        }
    }

    // Endpoint for getting video metadata
    @GetMapping("/{videoKey}")
    public ResponseEntity<?> getVideoMetadata(@PathVariable String videoKey) {
        try {
            Optional<VideoEntity> videoOpt = s3Service.getVideoByKey(videoKey);
            if (videoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            VideoEntity video = videoOpt.get();

            Map<String, Object> response = new HashMap<>();
            response.put("id", video.getId());
            response.put("videoKey", video.getVideoKey());
            response.put("title", video.getTitle());
            response.put("videoUrl", video.getVideoKey());
            response.put("thumbnailUrl", video.getThumbnailUrl());
            response.put("durationSeconds", video.getDurationSeconds());
            response.put("uploadedBy", Map.of(
                    "id", video.getUploadedBy().getId(),
                    "username", video.getUploadedBy().getName()
            ));
            response.put("metadata", video.getMetadata());
            response.put("isActive", video.getIsActive());
            response.put("createdAt", video.getCreatedAt());
            response.put("updatedAt", video.getUpdatedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get video metadata: " + e.getMessage()));
        }
    }

    // Endpoint for updating video metadata (after processing)
    @PutMapping("/{videoKey}/metadata")
    public ResponseEntity<?> updateVideoMetadata(
            @PathVariable String videoKey,
            @RequestParam(value = "durationSeconds", required = false) Integer durationSeconds,
            @RequestParam(value = "thumbnailUrl", required = false) String thumbnailUrl,
            @RequestParam(value = "processingResults", required = false) String processingResultsJson) {

        try {
            Map<String, Object> processingResults = null;

            if (processingResultsJson != null && !processingResultsJson.trim().isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonResults = objectMapper.readValue(processingResultsJson, Map.class);
                    processingResults = jsonResults;
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid JSON in processingResults"));
                }
            }

            VideoEntity updatedVideo = s3Service.updateVideoMetadata(videoKey, durationSeconds,
                    thumbnailUrl, processingResults);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Video metadata updated successfully");
            response.put("videoKey", updatedVideo.getVideoKey());
            response.put("durationSeconds", updatedVideo.getDurationSeconds());
            response.put("thumbnailUrl", updatedVideo.getThumbnailUrl());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to update metadata: " + e.getMessage()));
        }
    }

    // Endpoint for deleting a video
    @DeleteMapping("/{videoKey}")
    public ResponseEntity<?> deleteVideo(@PathVariable String videoKey) {
        try {
            // TODO: Add permission check here
            // Only allow video owner or admin to delete

            s3Service.deleteVideo(videoKey);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Video deleted successfully",
                    "videoKey", videoKey
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to delete video: " + e.getMessage()));
        }
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Video service is running"));
    }
}
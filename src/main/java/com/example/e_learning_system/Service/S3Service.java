package com.example.e_learning_system.Service;

import com.example.e_learning_system.Entities.Attachment;
import com.example.e_learning_system.Entities.VideoAttachments;
import com.example.e_learning_system.Entities.VideoEntity;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Repository.AttachmentRepository;
import com.example.e_learning_system.Repository.VideoAttachmentsRepository;
import com.example.e_learning_system.Repository.VideoRepository;
import com.example.e_learning_system.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final VideoAttachmentsRepository videoAttachmentsRepository;


    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.cloudfront.domain:}")
    private String cloudFrontDomain;

    public S3Service(S3Client s3Client, S3Presigner s3Presigner,
                     VideoRepository videoRepository, UserRepository userRepository, AttachmentRepository attachmentRepository, VideoAttachmentsRepository videoAttachmentsRepository) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.videoAttachmentsRepository = videoAttachmentsRepository;
    }

    // Upload file method with complete video entity creation
    @Transactional
    public VideoEntity uploadVideo(MultipartFile file, String title, Integer uploadedByUserId,
                                   String description, Map<String, Object> additionalMetadata) {
        try {
            // Generate unique key for the video
            String key = UUID.randomUUID() + "-" + sanitizeFileName(file.getOriginalFilename());

            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentLength(file.getSize())
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Get the user who uploaded the video
            UserEntity uploadedBy = userRepository.findById(uploadedByUserId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + uploadedByUserId));

            VideoEntity videoEntity = new VideoEntity();
            videoEntity.setVideoKey(key);
            videoEntity.setTitle(title != null ? title : file.getOriginalFilename());
            videoEntity.setUploadedBy(uploadedBy);
            videoEntity.setIsActive(true);

            // Build metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("originalFileName", file.getOriginalFilename());
            metadata.put("fileSize", file.getSize());
            metadata.put("contentType", file.getContentType());
            metadata.put("uploadTimestamp", System.currentTimeMillis());
            if (description != null) {
                metadata.put("description", description);
            }

            // Add any additional metadata provided
            if (additionalMetadata != null) {
                metadata.putAll(additionalMetadata);
            }

            videoEntity.setMetadata(metadata);

            // Save to database
            return videoRepository.save(videoEntity);

        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to S3", e);
        }
    }

    // Simplified upload method for basic use cases
    @Transactional
    public VideoEntity uploadVideo(MultipartFile file, String title, Integer uploadedByUserId) {
        return uploadVideo(file, title, uploadedByUserId, null, null);
    }

    // Method to update video metadata after processing
    //TODO convert the parameter to update video dto ...
    @Transactional
    public VideoEntity updateVideoMetadata(String videoKey, Integer durationSeconds,
                                           String thumbnailUrl, Map<String, Object> processingResults) {
        Optional<VideoEntity> videoOpt = videoRepository.findByVideoKey(videoKey);

        if (videoOpt.isEmpty()) {
            throw new RuntimeException("Video not found with key: " + videoKey);
        }

        VideoEntity video = videoOpt.get();

        // Update duration if provided
        if (durationSeconds != null) {
            video.setDurationSeconds(durationSeconds);
        }

        // Update thumbnail URL if provided
        if (thumbnailUrl != null) {
            video.setThumbnailUrl(thumbnailUrl);
        }

        // Update metadata with processing results
        if (processingResults != null) {
            Map<String, Object> existingMetadata = video.getMetadata();
            if (existingMetadata == null) {
                existingMetadata = new HashMap<>();
            }
            existingMetadata.putAll(processingResults);
            video.setMetadata(existingMetadata);
        }

        return videoRepository.save(video);
    }

    // Generate pre-signed URL valid for specified duration
    public String generatePresignedUrl(String key, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();

        URL url = s3Presigner.presignGetObject(presignRequest).url();
        return url.toString();
    }

    // Generate pre-signed URL valid for 5 minutes (backward compatibility)
    public String generatePresignedUrl(String key) {
        return generatePresignedUrl(key, Duration.ofMinutes(5));
    }

    // Delete video from S3 and database
    @Transactional
    public void deleteVideo(String videoKey) {
        try {
            // Delete from S3
            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(videoKey));

            // Delete from database
            videoRepository.deleteByVideoKey(videoKey);

        } catch (Exception e) {
            throw new RuntimeException("Error deleting video: " + videoKey, e);
        }
    }

    // Get video entity by key
    public Optional<VideoEntity> getVideoByKey(String videoKey) {
        return videoRepository.findByVideoKey(videoKey);
    }

    // Helper method to build video URL
    private String buildVideoUrl(String key) {
        if (cloudFrontDomain != null && !cloudFrontDomain.isEmpty()) {
            return "https://" + cloudFrontDomain + "/" + key;
        } else {
            return "https://" + bucketName + ".s3.amazonaws.com/" + key;
        }
    }

    // Helper method to sanitize file names
    private String sanitizeFileName(String originalFilename) {
        if (originalFilename == null) {
            return "video";
        }
        return originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public void close() {
        s3Presigner.close();
    }

    @Transactional
    void addAttachemntToVideo(int videoId , int attachmentId) {

        Optional<Attachment> attachment = attachmentRepository.findById(attachmentId);
        if (attachment.isEmpty()) {
            throw new RuntimeException("Attachment not found with ID: " + attachmentId);
        }
        Optional<VideoEntity> videoEntity = videoRepository.findById(videoId);
        if (videoEntity.isEmpty()) {
            throw new RuntimeException("Video not found with ID: " + videoId);
        }
        if(videoAttachmentsRepository.existsByVideoAndAttachment(videoEntity.get(), attachment.get())){
            throw new RuntimeException("Video already attached to an attachment");
        }else {
            VideoAttachments vidoeAttachments = new VideoAttachments();
            vidoeAttachments.setVideo(videoEntity.get());
            vidoeAttachments.setAttachment(attachment.get());
            videoEntity.get().addVideoAttachments(vidoeAttachments);
            videoRepository.save(videoEntity.get());
        }

    }
    @Transactional
    void removeAttachemntFromVideo(int videoId , int attachmentId) {
        Optional<Attachment> attachment = attachmentRepository.findById(attachmentId);
        if (attachment.isEmpty()) {
            throw new RuntimeException("Attachment not found with ID: " + attachmentId);
        }
        Optional<VideoEntity> videoEntity = videoRepository.findById(videoId);
        if (videoEntity.isEmpty()) {
            throw new RuntimeException("Video not found with ID: " + videoId);
        }
        videoEntity.get().removeVideoAttachmentByids(videoId, attachmentId);
        videoRepository.save(videoEntity.get());
    }


}
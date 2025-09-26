package com.example.e_learning_system.Service;

import com.example.e_learning_system.Entities.Attachment;
import com.example.e_learning_system.Entities.VideoAttachments;
import com.example.e_learning_system.Entities.VideoEntity;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Repository.AttachmentRepository;
import com.example.e_learning_system.Repository.VideoAttachmentsRepository;
import com.example.e_learning_system.Repository.VideoRepository;
import com.example.e_learning_system.Repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private VideoAttachmentsRepository videoAttachmentsRepository;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private VideoEntity videoEntity;

    @Mock
    private UserEntity userEntity;

    @Mock
    private Attachment attachment;

    @Mock
    private VideoAttachments videoAttachments;

    @InjectMocks
    private S3Service s3Service;

    private String bucketName = "test-bucket";
    private String cloudFrontDomain = "test.cloudfront.net";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", bucketName);
        ReflectionTestUtils.setField(s3Service, "cloudFrontDomain", cloudFrontDomain);
    }

    @Test
    void uploadVideo_ShouldUploadVideoSuccessfully_WhenValidInputs() throws IOException {
        // Arrange
        String title = "Test Video";
        Integer uploadedByUserId = 1;
        String description = "Test Description";
        Map<String, Object> additionalMetadata = new HashMap<>();
        additionalMetadata.put("category", "educational");

        byte[] fileContent = "test video content".getBytes();
        String originalFilename = "test-video.mp4";
        String contentType = "video/mp4";
        long fileSize = fileContent.length;

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getContentType()).thenReturn(contentType);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));

        when(userRepository.findById(uploadedByUserId)).thenReturn(Optional.of(userEntity));
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());
        when(videoRepository.save(any(VideoEntity.class))).thenReturn(videoEntity);

        // Act
        VideoEntity result = s3Service.uploadVideo(multipartFile, title, uploadedByUserId, description, additionalMetadata);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById(uploadedByUserId);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(videoRepository).save(any(VideoEntity.class));
    }

    @Test
    void uploadVideo_ShouldUploadVideoSuccessfully_WhenMinimalInputs() throws IOException {
        // Arrange
        String title = "Test Video";
        Integer uploadedByUserId = 1;

        byte[] fileContent = "test video content".getBytes();
        String originalFilename = "test-video.mp4";
        String contentType = "video/mp4";
        long fileSize = fileContent.length;

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getContentType()).thenReturn(contentType);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));

        when(userRepository.findById(uploadedByUserId)).thenReturn(Optional.of(userEntity));
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());
        when(videoRepository.save(any(VideoEntity.class))).thenReturn(videoEntity);

        // Act
        VideoEntity result = s3Service.uploadVideo(multipartFile, title, uploadedByUserId);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById(uploadedByUserId);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(videoRepository).save(any(VideoEntity.class));
    }

    @Test
    void uploadVideo_ShouldThrowException_WhenUserNotFound() throws IOException {
        // Arrange
        String title = "Test Video";
        Integer uploadedByUserId = 1;

        when(multipartFile.getOriginalFilename()).thenReturn("test-video.mp4");
        when(multipartFile.getContentType()).thenReturn("video/mp4");
        when(multipartFile.getSize()).thenReturn(1000L);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        when(userRepository.findById(uploadedByUserId)).thenReturn(Optional.empty());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // Act & Assert
        assertThatThrownBy(() -> s3Service.uploadVideo(multipartFile, title, uploadedByUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with ID: 1");

        verify(userRepository).findById(uploadedByUserId);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(videoRepository, never()).save(any(VideoEntity.class));
    }

    @Test
    void uploadVideo_ShouldThrowException_WhenIOException() throws IOException {
        // Arrange
        String title = "Test Video";
        Integer uploadedByUserId = 1;

        when(multipartFile.getOriginalFilename()).thenReturn("test-video.mp4");
        when(multipartFile.getContentType()).thenReturn("video/mp4");
        when(multipartFile.getSize()).thenReturn(1000L);
        when(multipartFile.getInputStream()).thenThrow(new IOException("Stream error"));

        // Act & Assert
        assertThatThrownBy(() -> s3Service.uploadVideo(multipartFile, title, uploadedByUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error uploading file to S3");

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(videoRepository, never()).save(any(VideoEntity.class));
    }

    @Test
    void updateVideoMetadata_ShouldUpdateMetadata_WhenVideoExists() {
        // Arrange
        String videoKey = "test-video-key";
        Integer durationSeconds = 120;
        String thumbnailUrl = "https://example.com/thumbnail.jpg";
        Map<String, Object> processingResults = new HashMap<>();
        processingResults.put("quality", "HD");

        Map<String, Object> existingMetadata = new HashMap<>();
        existingMetadata.put("originalFileName", "test.mp4");

        when(videoRepository.findByVideoKey(videoKey)).thenReturn(Optional.of(videoEntity));
        when(videoEntity.getMetadata()).thenReturn(existingMetadata);
        when(videoRepository.save(videoEntity)).thenReturn(videoEntity);

        // Act
        VideoEntity result = s3Service.updateVideoMetadata(videoKey, durationSeconds, thumbnailUrl, processingResults);

        // Assert
        assertThat(result).isNotNull();
        verify(videoRepository).findByVideoKey(videoKey);
        verify(videoEntity).setDurationSeconds(durationSeconds);
        verify(videoEntity).setThumbnail(attachment);
        verify(videoEntity).setMetadata(any());
        verify(videoRepository).save(videoEntity);
    }

    @Test
    void updateVideoMetadata_ShouldThrowException_WhenVideoNotFound() {
        // Arrange
        String videoKey = "non-existent-key";

        when(videoRepository.findByVideoKey(videoKey)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> s3Service.updateVideoMetadata(videoKey, 120, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Video not found with key: non-existent-key");

        verify(videoRepository).findByVideoKey(videoKey);
        verify(videoRepository, never()).save(any(VideoEntity.class));
    }

    @Test
    void generatePresignedUrl_ShouldReturnPresignedUrl_WhenValidKey() throws Exception {
        // Arrange
        String key = "test-video-key";
        Duration duration = Duration.ofMinutes(10);
        String expectedUrlString = "https://example.com/presigned-url";

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(java.net.URI.create(expectedUrlString).toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        // Act
        String result = s3Service.generatePresignedUrl(key, duration);

        // Assert
        assertThat(result).isEqualTo(expectedUrlString);
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void generatePresignedUrl_ShouldReturnPresignedUrl_WhenDefaultDuration() throws Exception {
        // Arrange
        String key = "test-video-key";
        String expectedUrlString = "https://example.com/presigned-url";

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(java.net.URI.create(expectedUrlString).toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        // Act
        String result = s3Service.generatePresignedUrl(key);

        // Assert
        assertThat(result).isEqualTo(expectedUrlString);
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void deleteVideo_ShouldDeleteVideoSuccessfully_WhenValidKey() {
        // Arrange
        String videoKey = "test-video-key";

        // Act
        s3Service.deleteVideo(videoKey);

        // Assert
        verify(videoRepository).deleteByVideoKey(videoKey);
    }

    @Test
    void deleteVideo_ShouldThrowException_WhenDatabaseDeleteFails() {
        // Arrange
        String videoKey = "test-video-key";
        
        doThrow(new RuntimeException("Database delete failed")).when(videoRepository).deleteByVideoKey(videoKey);

        // Act & Assert
        assertThatThrownBy(() -> s3Service.deleteVideo(videoKey))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error deleting video: test-video-key");
    }

    @Test
    void getVideoByKey_ShouldReturnVideo_WhenVideoExists() {
        // Arrange
        String videoKey = "test-video-key";
        
        when(videoRepository.findByVideoKey(videoKey)).thenReturn(Optional.of(videoEntity));

        // Act
        Optional<VideoEntity> result = s3Service.getVideoByKey(videoKey);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(videoEntity);
        verify(videoRepository).findByVideoKey(videoKey);
    }

    @Test
    void getVideoByKey_ShouldReturnEmpty_WhenVideoNotExists() {
        // Arrange
        String videoKey = "non-existent-key";
        
        when(videoRepository.findByVideoKey(videoKey)).thenReturn(Optional.empty());

        // Act
        Optional<VideoEntity> result = s3Service.getVideoByKey(videoKey);

        // Assert
        assertThat(result).isEmpty();
        verify(videoRepository).findByVideoKey(videoKey);
    }

    @Test
    void addAttachmentToVideo_ShouldAddAttachment_WhenValidIds() {
        // Arrange
        int videoId = 1;
        int attachmentId = 1;

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(videoEntity));
        when(videoAttachmentsRepository.existsByVideoAndAttachment(videoEntity, attachment)).thenReturn(false);
        when(videoRepository.save(videoEntity)).thenReturn(videoEntity);

        // Act
        s3Service.addAttachemntToVideo(videoId, attachmentId);

        // Assert
        verify(attachmentRepository).findById(attachmentId);
        verify(videoRepository).findById(videoId);
        verify(videoAttachmentsRepository).existsByVideoAndAttachment(videoEntity, attachment);
        verify(videoEntity).addVideoAttachments(any(VideoAttachments.class));
        verify(videoRepository).save(videoEntity);
    }

    @Test
    void addAttachmentToVideo_ShouldThrowException_WhenAttachmentNotFound() {
        // Arrange
        int videoId = 1;
        int attachmentId = 1;

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> s3Service.addAttachemntToVideo(videoId, attachmentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Attachment not found with ID: 1");

        verify(attachmentRepository).findById(attachmentId);
        verify(videoRepository, never()).findById(anyInt());
    }

    @Test
    void addAttachmentToVideo_ShouldThrowException_WhenVideoNotFound() {
        // Arrange
        int videoId = 1;
        int attachmentId = 1;

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> s3Service.addAttachemntToVideo(videoId, attachmentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Video not found with ID: 1");

        verify(attachmentRepository).findById(attachmentId);
        verify(videoRepository).findById(videoId);
        verify(videoAttachmentsRepository, never()).existsByVideoAndAttachment(any(), any());
    }

    @Test
    void addAttachmentToVideo_ShouldThrowException_WhenAttachmentAlreadyExists() {
        // Arrange
        int videoId = 1;
        int attachmentId = 1;

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(videoEntity));
        when(videoAttachmentsRepository.existsByVideoAndAttachment(videoEntity, attachment)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> s3Service.addAttachemntToVideo(videoId, attachmentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Video already attached to an attachment");

        verify(attachmentRepository).findById(attachmentId);
        verify(videoRepository).findById(videoId);
        verify(videoAttachmentsRepository).existsByVideoAndAttachment(videoEntity, attachment);
        verify(videoEntity, never()).addVideoAttachments(any());
        verify(videoRepository, never()).save(any());
    }

    @Test
    void removeAttachmentFromVideo_ShouldRemoveAttachment_WhenValidIds() {
        // Arrange
        int videoId = 1;
        int attachmentId = 1;

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(videoEntity));
        when(videoRepository.save(videoEntity)).thenReturn(videoEntity);

        // Act
        s3Service.removeAttachemntFromVideo(videoId, attachmentId);

        // Assert
        verify(attachmentRepository).findById(attachmentId);
        verify(videoRepository).findById(videoId);
        verify(videoEntity).removeVideoAttachmentByids(videoId, attachmentId);
        verify(videoRepository).save(videoEntity);
    }

    @Test
    void removeAttachmentFromVideo_ShouldThrowException_WhenAttachmentNotFound() {
        // Arrange
        int videoId = 1;
        int attachmentId = 1;

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> s3Service.removeAttachemntFromVideo(videoId, attachmentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Attachment not found with ID: 1");

        verify(attachmentRepository).findById(attachmentId);
        verify(videoRepository, never()).findById(anyInt());
    }

    @Test
    void removeAttachmentFromVideo_ShouldThrowException_WhenVideoNotFound() {
        // Arrange
        int videoId = 1;
        int attachmentId = 1;

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> s3Service.removeAttachemntFromVideo(videoId, attachmentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Video not found with ID: 1");

        verify(attachmentRepository).findById(attachmentId);
        verify(videoRepository).findById(videoId);
        verify(videoEntity, never()).removeVideoAttachmentByids(anyInt(), anyInt());
    }

    @Test
    void close_ShouldCloseS3Presigner() {
        // Act
        s3Service.close();

        // Assert
        verify(s3Presigner).close();
    }
}

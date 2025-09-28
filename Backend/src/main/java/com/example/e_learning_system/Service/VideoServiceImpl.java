package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.VideoDtos.CreatVideoDto;
import com.example.e_learning_system.Dto.VideoDtos.VideoDto;
import com.example.e_learning_system.Entities.VideoEntity;
import com.example.e_learning_system.Mapper.VideoMapper;
import com.example.e_learning_system.Repository.AttachmentRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.Repository.VideoEntityRepository;
import com.example.e_learning_system.Service.Interfaces.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoEntityRepository videoEntityRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final AttachmentRepository attachmentRepository;

    @Override
    @Transactional
    public VideoDto uploadVideo(MultipartFile file, CreatVideoDto createVideoDto, Integer uploadedById) {
        // Use S3Service to upload and create entity
        VideoEntity videoEntity = s3Service.uploadVideo(file, createVideoDto.getTitle(), uploadedById,
                null, null); // Adjust metadata if needed
        // Set additional fields from DTO
        var uploadedBy = userRepository.findById(uploadedById)
                .orElseThrow(() -> new RuntimeException("UploadedBy user not found"));
        videoEntity.setUploadedBy(uploadedBy);
        videoEntity.setIsActive(true);
        videoEntity.setExplanation(createVideoDto.getExplanation());
        videoEntity.setWhatWeWillLearn(createVideoDto.getWhatWeWillLearn());
        videoEntity.setStatus(createVideoDto.getStatus());
        videoEntity.setPrerequisites(createVideoDto.getPrerequisites());

        if (createVideoDto.getThumbnail() != null) {
            var thumbnail = attachmentRepository.findById(createVideoDto.getThumbnail());
            if (thumbnail.isEmpty()) {
                throw new RuntimeException("Thumbnail attachment not found");
            }
            videoEntity.setThumbnail(thumbnail.get());
        } else {
            throw new RuntimeException("Thumbnail is required");
        }
        // Handle attachments if provided
        if (createVideoDto.getAttachments() != null && !createVideoDto.getAttachments().isEmpty()) {
            var attachments = attachmentRepository.findAllById(createVideoDto.getAttachments());
            if (attachments.size() != createVideoDto.getAttachments().size()) {
                throw new RuntimeException("One or more attachments not found");
            }
            videoEntity.setAttachments(attachments);
        }

        // Update additional fields from DTO
        videoEntity = videoEntityRepository.save(videoEntity);

        return VideoMapper.fromVideoEntityToVideoDto(videoEntity);
    }

    @Override
    public VideoDto getVideoById(Integer id) {
        VideoEntity videoEntity = videoEntityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        return VideoMapper.fromVideoEntityToVideoDto(videoEntity);
    }

    @Override
    public Page<VideoDto> getVideos(Pageable pageable) {
        Page<VideoEntity> videoEntities = videoEntityRepository.findAll(pageable);
        return videoEntities.map(VideoMapper::fromVideoEntityToVideoDto);
    }

    @Override
    public List<VideoDto> getVideosByUser(Integer userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<VideoEntity> videoEntities = videoEntityRepository.findByUploadedByAndIsActive(user, true);
        return videoEntities.stream()
                .map(VideoMapper::fromVideoEntityToVideoDto)
                .toList();
    }

    @Override
    @Transactional
    public VideoDto updateVideo(Integer id, CreatVideoDto updateVideoDto) {
        VideoEntity videoEntity = videoEntityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        VideoMapper.updateVideoEntityFromCreatVideoDto(videoEntity, updateVideoDto);
        videoEntity = videoEntityRepository.save(videoEntity);

        return VideoMapper.fromVideoEntityToVideoDto(videoEntity);
    }

    @Override
    @Transactional
    public void deleteVideo(Integer id) {
        VideoEntity videoEntity = videoEntityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        // Delete from S3
        s3Service.deleteVideo(videoEntity.getVideoKey());

        // Delete from DB
        videoEntityRepository.delete(videoEntity);
    }

    @Override
    public String getVideoUrl(Integer id, Integer durationMinutes) {
        VideoEntity videoEntity = videoEntityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        return s3Service.generatePresignedUrl(videoEntity.getVideoKey(), Duration.ofMinutes(durationMinutes));
    }


    @Override
    @Transactional
    public void addAttachmentToVideo(Integer videoId, Integer attachmentId) {
        s3Service.addAttachemntToVideo(videoId, attachmentId);
    }

    @Override
    @Transactional
    public void removeAttachmentFromVideo(Integer videoId, Integer attachmentId) {
        s3Service.removeAttachemntFromVideo(videoId, attachmentId);
    }

}
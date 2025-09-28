package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.ApiResponse;
import com.example.e_learning_system.Dto.VideoDtos.CreatVideoDto;
import com.example.e_learning_system.Dto.VideoDtos.VideoDto;
import com.example.e_learning_system.Service.Interfaces.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /**
     * Upload a new video
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<VideoDto>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("durationSeconds") int durationSeconds,
            @RequestParam(value = "explanation", required = false) String explanation,
            @RequestParam(value = "whatWeWillLearn", required = false) String whatWeWillLearn,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "prerequisites", required = false) String prerequisites,
            @RequestParam(value = "thumbnail", required = false) Integer thumbnail,
            @RequestParam(value = "attachments", required = false) Set<Integer> attachments
            ) {

        CreatVideoDto createVideoDto = CreatVideoDto.builder()
                .title(title)
                .durationSeconds(durationSeconds)
                .createdByUserId(1) // TODO: Replace with actual user ID from auth context
                .explanation(explanation)
                .whatWeWillLearn(whatWeWillLearn)
                .status(status)
                .prerequisites(prerequisites)
                .thumbnail(thumbnail)
                .attachments(attachments)
                .build();

        VideoDto videoDto = videoService.uploadVideo(file, createVideoDto, 1);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Video uploaded successfully", videoDto));
    }    /**
     * Get video by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoDto>> getVideo(@PathVariable Integer id) {
        VideoDto video = videoService.getVideoById(id);
        return ResponseEntity.ok(ApiResponse.success("Video retrieved successfully", video));
    }

    /**
     * Get all videos with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<VideoDto>>> getVideos(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<VideoDto> videos = videoService.getVideos(pageable);
        return ResponseEntity.ok(ApiResponse.success("Videos retrieved successfully", videos));
    }

    /**
     * Get videos by user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<VideoDto>>> getVideosByUser(@PathVariable Integer userId) {
        List<VideoDto> videos = videoService.getVideosByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User videos retrieved successfully", videos));
    }

    /**
     * Update video
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoDto>> updateVideo(
            @PathVariable Integer id,
            @Valid @RequestBody CreatVideoDto updateVideoDto,
            @RequestParam("video") MultipartFile videoFile
            ) {
        VideoDto video = videoService.updateVideo(id, updateVideoDto);
        if(videoFile != null && !videoFile.isEmpty()) {
            videoService.deleteVideo(id);
            video = videoService.uploadVideo(videoFile, updateVideoDto, 1);
        }
        return ResponseEntity.ok(ApiResponse.success("Video updated successfully", video));
    }

    /**
     * Delete video
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable Integer id) {
        videoService.deleteVideo(id);
        return ResponseEntity.ok(ApiResponse.success("Video deleted successfully", null));
    }

    /**
     * Get video URL for viewing
     */
    @GetMapping("/{id}/url")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVideoUrl(
            @PathVariable Integer id,
            @RequestParam(value = "duration", defaultValue = "5") Integer durationMinutes) {

        String presignedUrl = videoService.getVideoUrl(id, durationMinutes);

        Map<String, Object> response = new HashMap<>();
        response.put("videoId", id);
        response.put("presignedUrl", presignedUrl);
        response.put("expiresInMinutes", durationMinutes);

        return ResponseEntity.ok(ApiResponse.success("Video URL generated successfully", response));
    }

    @PostMapping("/{videoId}/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> addAttachmentToVideo(
            @PathVariable Integer videoId,
            @PathVariable Integer attachmentId) {
        videoService.addAttachmentToVideo(videoId, attachmentId);
        return ResponseEntity.ok(ApiResponse.success("Attachment added to video successfully", null));
    }

    @DeleteMapping("/{videoId}/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> removeAttachmentFromVideo(
            @PathVariable Integer videoId,
            @PathVariable Integer attachmentId) {
        videoService.removeAttachmentFromVideo(videoId, attachmentId);
        return ResponseEntity.ok(ApiResponse.success("Attachment removed from video successfully", null));
    }


    
}
package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.ApiResponse;
import com.example.e_learning_system.Dto.VideoDtos.CreatVideoDto;
import com.example.e_learning_system.Dto.VideoDtos.VideoDto;
import com.example.e_learning_system.Service.Interfaces.VideoService;
import com.example.e_learning_system.Service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final VideoService videoService;
    private final AuthorizationService authorizationService;

    /**
        * Create lesson - Only teachers and admins can create lessons
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<VideoDto>> createLesson(
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

        // Only teachers and admins can create lessons
        authorizationService.requireTeacherOrAdmin();
        
        Integer currentUserId = authorizationService.getCurrentUser().getId();

        CreatVideoDto createVideoDto = CreatVideoDto.builder()
                .title(title)
                .durationSeconds(durationSeconds)
                .createdByUserId(currentUserId)
                .explanation(explanation)
                .whatWeWillLearn(whatWeWillLearn)
                .status(status)
                .prerequisites(prerequisites)
                .thumbnail(thumbnail)
                .attachments(attachments)
                .build();

        VideoDto videoDto = videoService.uploadVideo(file, createVideoDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lesson created successfully", videoDto));
    }

    /**
     * Get lesson by ID - Only teachers and admins can view lesson content
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoDto>> getLesson(@PathVariable Integer id) {
        // Only teachers and admins can view lesson content (students cannot as per requirements)
        authorizationService.requireVideoViewAccess();
        
        VideoDto video = videoService.getVideoById(id);
        return ResponseEntity.ok(ApiResponse.success("Lesson retrieved successfully", video));
    }

    /**
     * Get all lessons with pagination - Only teachers and admins can view lessons
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<VideoDto>>> getLessons(
            @PageableDefault(size = 20) Pageable pageable) {
        // Only teachers and admins can view lessons
        authorizationService.requireVideoViewAccess();
        
        Page<VideoDto> videos = videoService.getVideos(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lessons retrieved successfully", videos));
    }

    /**
     * Update lesson
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoDto>> updateLesson(
            @PathVariable Integer id,
            @RequestParam("title") String title,
            @RequestParam("durationSeconds") int durationSeconds,
            @RequestParam(value = "explanation", required = false) String explanation,
            @RequestParam(value = "whatWeWillLearn", required = false) String whatWeWillLearn,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "prerequisites", required = false) String prerequisites,
            @RequestParam(value = "thumbnail", required = false) Integer thumbnail,
            @RequestParam(value = "attachments", required = false) Set<Integer> attachments,
            @RequestParam(value = "video", required = false) MultipartFile videoFile
            ) {

        CreatVideoDto updateVideoDto = CreatVideoDto.builder()
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

        VideoDto video = videoService.updateVideo(id, updateVideoDto);
        if(videoFile != null && !videoFile.isEmpty()) {
            videoService.deleteVideo(id);
            video = videoService.uploadVideo(videoFile, updateVideoDto, 1);
        }
        return ResponseEntity.ok(ApiResponse.success("Lesson updated successfully", video));
    }

    /**
     * Delete lesson
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable Integer id) {
        videoService.deleteVideo(id);
        return ResponseEntity.ok(ApiResponse.success("Lesson deleted successfully", null));
    }

    /**
     * Add attachment to lesson
     */
    @PostMapping("/{lessonId}/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> addAttachmentToLesson(
            @PathVariable Integer lessonId,
            @PathVariable Integer attachmentId) {
        videoService.addAttachmentToVideo(lessonId, attachmentId);
        return ResponseEntity.ok(ApiResponse.success("Attachment added to lesson successfully", null));
    }

    /**
     * Remove attachment from lesson
     */
    @DeleteMapping("/{lessonId}/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> removeAttachmentFromLesson(
            @PathVariable Integer lessonId,
            @PathVariable Integer attachmentId) {
        videoService.removeAttachmentFromVideo(lessonId, attachmentId);
        return ResponseEntity.ok(ApiResponse.success("Attachment removed from lesson successfully", null));
    }
}
package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Dto.VideoDtos.CreatVideoDto;
import com.example.e_learning_system.Dto.VideoDtos.VideoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {

    VideoDto uploadVideo(MultipartFile file, CreatVideoDto createVideoDto, Integer uploadedById);

    VideoDto getVideoById(Integer id);

    Page<VideoDto> getVideos(Pageable pageable);

    List<VideoDto> getVideosByUser(Integer userId);

    VideoDto updateVideo(Integer id, CreatVideoDto updateVideoDto);

    void deleteVideo(Integer id);

    String getVideoUrl(Integer id, Integer durationMinutes);
}
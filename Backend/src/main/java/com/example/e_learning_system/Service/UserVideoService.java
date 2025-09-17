package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.VideoDtos.VideoDto;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Entities.VideoEntity;
import com.example.e_learning_system.Mapper.VideoMapper;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.Repository.VideoRepository;
import com.example.e_learning_system.Security.UserUtil;
import com.example.e_learning_system.Service.Interfaces.UserVideo;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserVideoService implements UserVideo {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    @Override
    @Transactional
    public void markedWatched(Integer videoId) {
        Integer userId = UserUtil.getCurrentUserId().intValue();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        VideoEntity video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        if (!user.getWatchedVideos().contains(video)) {
            user.getWatchedVideos().add(video);
        }
    }

    @Override
    @Transactional
    public void unMarkedWatched(Integer videoId) {
        Integer userId = UserUtil.getCurrentUserId().intValue();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        VideoEntity video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        if(user.getWatchedVideos().contains(video)) {
            user.getWatchedVideos().remove(video);
        }
    }

    @Override
    public Set<VideoDto> getWatchedVideosByUser(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getWatchedVideos().stream()
                .map(VideoMapper::fromVideoEntityToVideoDto)
                .collect(Collectors.toSet());
    }
}

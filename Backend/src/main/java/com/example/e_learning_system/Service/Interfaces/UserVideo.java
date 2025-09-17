package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Dto.VideoDtos.VideoDto;

import java.util.Set;

public interface UserVideo {
    void markedWatched(Integer videoId);
    void unMarkedWatched(Integer videoId);
    Set<VideoDto> getWatchedVideosByUser(Integer userId);

}

package com.example.e_learning_system.Controller;
import com.example.e_learning_system.Dto.UserVideoDTO;
import com.example.e_learning_system.Dto.VideoDtos.VideoDto;
import com.example.e_learning_system.Entities.VideoEntity;
import com.example.e_learning_system.Mapper.VideoMapper;
import com.example.e_learning_system.Service.Interfaces.UserVideo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserVideoController {

    private final UserVideo userVideo;

    @PostMapping("watchVideo")
    public ResponseEntity<String> markVideoAsWatched(@Valid @RequestBody UserVideoDTO userVideoDTO) {
        userVideo.markedWatched(userVideoDTO.getVideoId());
        return ResponseEntity.ok("Video marked as watched.");
    }
@DeleteMapping("unWatchVideo")
    public ResponseEntity<String> UnMarkVideoAsWatched(@Valid @RequestBody UserVideoDTO userVideoDTO) {
        userVideo.unMarkedWatched(userVideoDTO.getVideoId());
        return ResponseEntity.ok("Video marked as un watched.");
    }

    @GetMapping("watched/{userId}")
    public ResponseEntity<Set<VideoDto>> getWatchedVideos(@PathVariable Integer userId) {
        Set<VideoDto> videos = userVideo.getWatchedVideosByUser(userId);
        return ResponseEntity.ok(videos);
    }
}

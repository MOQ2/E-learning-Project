package com.example.e_learning_system.Service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import com.example.e_learning_system.Dto.ModuleDtos.CreateModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.DetailedModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.UpdateModuleDto;
import com.example.e_learning_system.Dto.OrderDtos.IdOrderDto;
import com.example.e_learning_system.Entities.ModuleVideos;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Entities.VideoEntity;
import com.example.e_learning_system.Service.Interfaces.ModuleService;
import com.example.e_learning_system.Mapper.ModuleMapper;
import com.example.e_learning_system.Mapper.VideoMapper;
import com.example.e_learning_system.Repository.ModuleRepository;
import com.example.e_learning_system.Repository.ModuleVideosRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.Repository.VideoRepository;
import com.example.e_learning_system.Security.UserUtil;
import com.example.e_learning_system.Entities.Module;

@Service
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final VideoRepository videoRepository;
    private final ModuleVideosRepository moduleVideosRepository;
    private final UserRepository userRepository;

    public ModuleServiceImpl(ModuleRepository moduleRepository, VideoRepository videoRepository, ModuleVideosRepository moduleVideosRepository, UserRepository userRepository) {
        this.moduleRepository = moduleRepository;
        this.videoRepository = videoRepository;
        this.moduleVideosRepository = moduleVideosRepository;
        this.userRepository = userRepository;
    }

    @Override
    public int createModule(CreateModuleDto createModuleDto) {
        Module module = ModuleMapper.fromCreateModuleDtoToModule(createModuleDto);
        // Set createdBy
        Long userId = UserUtil.getCurrentUserId();
        if (userId != null) {
            UserEntity user = new UserEntity();
            user.setId(userId.intValue());
            module.setCreatedBy(user);
        }
        // TODO: Remove the following line after implementing proper user retrieval
        userRepository.findById(1).ifPresent(module::setCreatedBy); // Temporary hardcoded user assignment
        Module savedModule = moduleRepository.save(module);
        return savedModule.getId();
    }


    @Override
    public void deleteModule(int moduleId) {
        moduleRepository.deleteById(moduleId);
    }

    @Override
    public DetailedModuleDto getModule(int moduleId) {
        Optional<Module> module = moduleRepository.findById(moduleId);
        return module.map(ModuleMapper::fromModuleToDetailedModuleDto).orElse(null);
    }

    @Override
    public List<com.example.e_learning_system.Dto.VideoDtos.VideoDto> getModuleLessons(int moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        List<ModuleVideos> moduleVideos = moduleVideosRepository.findByModuleOrderByVideoOrderAsc(module);
        return VideoMapper.fromModuleVideosToVideoDtos(moduleVideos);
    }

    @Override
    public com.example.e_learning_system.Dto.VideoDtos.VideoDto getLessonInModule(int moduleId, int lessonId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        VideoEntity video = videoRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        // Find the ModuleVideos join entry to get the order
        ModuleVideos moduleVideo = moduleVideosRepository.findByModuleAndVideo(module, video)
                .orElseThrow(() -> new RuntimeException("Lesson not found in this module"));
        
        // Convert to VideoDto including the order from the join table
        return VideoMapper.fromModuleVideoToVideoDto(moduleVideo);
    }

    @Override
    public void addVideoToModule(int moduleId, int videoId , int order ) {
        if(order < 0 ){
            return;
        }
        Module module =moduleRepository.findById(moduleId).orElseThrow(()-> new RuntimeException("Module not found"));
        VideoEntity video = videoRepository.findById(videoId).orElseThrow(()->new RuntimeException("video not found "));

        Optional<ModuleVideos> moduleVideo = moduleVideosRepository.findByModuleAndVideo(module,video);
        if(moduleVideo.isEmpty()){

            if (module.isUniqOrder(order)) {
                ModuleVideos newModuleVideo = new ModuleVideos();
                newModuleVideo.setVideoOrder(order);
                newModuleVideo.setModule(module);
                newModuleVideo.setVideo(video);
                moduleVideosRepository.save(newModuleVideo);
            }else {
                throw new RuntimeException("video order already exist");
            }

        }
    }

    @Override
    public void removeVideoFromModule(int moduleId, int videoId ) {
        Optional<ModuleVideos> moduleVideo = moduleVideosRepository.findByModuleIdAndVideoId(moduleId, videoId);
        moduleVideo.orElseThrow(() -> new RuntimeException("Video is not associated with this module"));

        moduleVideo.ifPresent(moduleVideosRepository::delete);
    }

    @Override
    public void updateVideoOrderInModule(int moduleId, int videoId, int newOrder) {
        updateVideoOrdersInModule(moduleId, Collections.singletonList(new IdOrderDto(videoId, newOrder)));
    }

    @Override
    public void updateVideoOrdersInModule(int moduleId, List<IdOrderDto> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        List<ModuleVideos> moduleVideos = moduleVideosRepository.findByModule(module);
        if (moduleVideos.isEmpty()) {
            return;
        }

        Map<Integer, ModuleVideos> moduleVideoByVideoId = moduleVideos.stream()
                .collect(Collectors.toMap(mv -> mv.getVideo().getId(), mv -> mv));

        Map<Integer, Integer> requestedOrders = new HashMap<>();
        for (IdOrderDto od : orders) {
            if (od.getId() == null) {
                throw new RuntimeException("Video id is required");
            }
            if (!moduleVideoByVideoId.containsKey(od.getId())) {
                throw new RuntimeException("Video with id " + od.getId() + " not found in module");
            }
            if (od.getOrder() == null || od.getOrder() < 0) {
                throw new RuntimeException("Invalid order value");
            }
            Integer previous = requestedOrders.putIfAbsent(od.getId(), od.getOrder());
            if (previous != null && !previous.equals(od.getOrder())) {
                throw new RuntimeException("Conflicting orders supplied for the same video");
            }
        }

        Set<Integer> finalOrders = new HashSet<>();
        for (ModuleVideos moduleVideo : moduleVideos) {
            int videoId = moduleVideo.getVideo().getId();
            int newOrder = requestedOrders.getOrDefault(videoId, moduleVideo.getVideoOrder());
            if (!finalOrders.add(newOrder)) {
                throw new RuntimeException("Duplicate video order detected");
            }
            moduleVideo.setVideoOrder(newOrder);
        }

        moduleVideosRepository.saveAll(moduleVideos);
    }

    @Override
    public void updateModule(UpdateModuleDto updateModuleDto, int moduleId) {
        Optional<Module> module = moduleRepository.findById(moduleId);        
        if(module.isPresent()){
            Module moduleToUpdate = module.get();
            ModuleMapper.fromUpdateModuleDtoToModule(updateModuleDto, moduleToUpdate);
            moduleRepository.save(moduleToUpdate);
        }
    }
}

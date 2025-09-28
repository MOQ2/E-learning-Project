package com.example.e_learning_system.Service;

import org.springframework.stereotype.Service;
import java.util.Optional;
import com.example.e_learning_system.Dto.ModuleDtos.CreateModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.DetailedModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.UpdateModuleDto;
import com.example.e_learning_system.Entities.ModuleVideos;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Entities.VideoEntity;
import com.example.e_learning_system.Service.Interfaces.ModuleService;
import com.example.e_learning_system.Mapper.ModuleMapper;
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
        if (newOrder < 0) return;
        ModuleVideos moduleVideo = moduleVideosRepository.findByModuleIdAndVideoId(moduleId, videoId)
                .orElseThrow(() -> new RuntimeException("Video not found in module"));

        // Check uniqueness
        Module module = moduleVideo.getModule();
        boolean exists = module.getModuleVideos().stream()
                .anyMatch(mv -> mv.getVideoOrder() == newOrder && mv.getVideo().getId() != videoId);
        if (exists) {
            throw new RuntimeException("video order already exist");
        }

        moduleVideo.setVideoOrder(newOrder);
        moduleVideosRepository.save(moduleVideo);
    }

    @Override
    public void updateVideoOrdersInModule(int moduleId, java.util.List<com.example.e_learning_system.Dto.OrderDtos.IdOrderDto> orders) {
        if (orders == null || orders.isEmpty()) return;

        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new RuntimeException("Module not found"));

        // Validate provided orders and ensure no duplicates in payload
        java.util.Set<Integer> seenOrders = new java.util.HashSet<>();
        for (com.example.e_learning_system.Dto.OrderDtos.IdOrderDto od : orders) {
            if (od.getOrder() == null || od.getOrder() < 0) throw new RuntimeException("Invalid order");
            if (!seenOrders.add(od.getOrder())) throw new RuntimeException("Duplicate order in payload");
        }

        // Apply updates
        for (com.example.e_learning_system.Dto.OrderDtos.IdOrderDto od : orders) {
            int videoId = od.getId();
            int newOrder = od.getOrder();
            ModuleVideos moduleVideo = moduleVideosRepository.findByModuleIdAndVideoId(moduleId, videoId)
                    .orElseThrow(() -> new RuntimeException("Video with id " + videoId + " not found in module"));

            // Check uniqueness against other module videos (excluding the current video)
            boolean exists = module.getModuleVideos().stream()
                    .anyMatch(mv -> mv.getVideoOrder() == newOrder && mv.getVideo().getId() != videoId);
            if (exists) {
                throw new RuntimeException("video order already exist");
            }
            moduleVideo.setVideoOrder(newOrder);
            moduleVideosRepository.save(moduleVideo);
        }
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

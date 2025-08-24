package com.example.e_learning_system.Service;

import com.example.e_learning_system.Dto.ModuleDtos.CreateModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.DetailedModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.UpdateModuleDto;
import com.example.e_learning_system.Entities.ModuleVideos;
import com.example.e_learning_system.Entities.VideoEntity;
import com.example.e_learning_system.Interfaces.ModuleService;
import com.example.e_learning_system.Mapper.ModuleMapper;
import com.example.e_learning_system.Repository.ModuleRepository;
import com.example.e_learning_system.Repository.ModuleVideosRepository;
import com.example.e_learning_system.Repository.VideoRepository;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.e_learning_system.Entities.Module;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final VideoRepository videoRepository;
    private final ModuleVideosRepository moduleVideosRepository;

    @Override
    public void createModule(CreateModuleDto createModuleDto) {
        Module module = ModuleMapper.fromCreateModuleDtoToModule(createModuleDto);
        moduleRepository.save(module);
    }

    @Override
    public void updateModule( UpdateModuleDto updateModuleDto , int moduleId) {
        Optional<Module> module = moduleRepository.findById(moduleId);
        if(module.isPresent()){
            Module moduleToUpdate = module.get();
            ModuleMapper.fromUpdateModuleDtoToModule(updateModuleDto, moduleToUpdate);
            moduleRepository.save(moduleToUpdate);
        }
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
        Module module =moduleRepository.findById(moduleId).orElseThrow(()-> ResourceNotFound.moduleNotFound(moduleId+""));
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

        Module module =moduleRepository.findById(moduleId).orElseThrow(()-> ResourceNotFound.moduleNotFound(moduleId+""));
        VideoEntity video = videoRepository.findById(videoId).orElseThrow(()->new RuntimeException("video not found "));

        Optional<ModuleVideos> moduleVideo = moduleVideosRepository.findByModuleAndVideo(module,video);

        if(moduleVideo.isPresent()){
            module.removeVideoFromModule(moduleVideo.get());
            moduleVideosRepository.save(moduleVideo.get());
        }
    }
}

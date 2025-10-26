package com.example.e_learning_system.Service.Interfaces;

import com.example.e_learning_system.Dto.ModuleDtos.CreateModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.DetailedModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.UpdateModuleDto;
import com.example.e_learning_system.Dto.OrderDtos.IdOrderDto;
import com.example.e_learning_system.Dto.VideoDtos.VideoDto;

import java.util.List;

public interface ModuleService {

    int createModule(CreateModuleDto createModuleDto);

    void updateModule(UpdateModuleDto updateModuleDto , int moduleId);

    void deleteModule(int moduleId);

    DetailedModuleDto getModule(int moduleId);

    List<VideoDto> getModuleLessons(int moduleId);

    VideoDto getLessonInModule(int moduleId, int lessonId);

    void addVideoToModule(int moduleId, int videoId , int order);

    void removeVideoFromModule(int moduleId, int videoId);

    void updateVideoOrderInModule(int moduleId, int videoId, int newOrder);

    void updateVideoOrdersInModule(int moduleId, List<IdOrderDto> orders);
}
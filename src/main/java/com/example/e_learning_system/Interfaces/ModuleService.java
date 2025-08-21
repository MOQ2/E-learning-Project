package com.example.e_learning_system.Interfaces;

import com.example.e_learning_system.Dto.ModuleDtos.CreateModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.DetailedModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.UpdateModuleDto;

public interface ModuleService {

    void createModule(CreateModuleDto createModuleDto);

    void updateModule(UpdateModuleDto updateModuleDto , int moduleId);

    void deleteModule(int moduleId);

    DetailedModuleDto getModule(int moduleId);

    void addVideoToModule(int videoId, int attachmentId , int order);

    void removeVideoFromModule(int videoId, int attachmentId);
}
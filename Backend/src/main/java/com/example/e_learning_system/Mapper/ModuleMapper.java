package com.example.e_learning_system.Mapper;

import com.example.e_learning_system.Dto.ModuleDtos.CreateModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.DetailedModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.ModuleSummaryDto;
import com.example.e_learning_system.Dto.ModuleDtos.UpdateModuleDto;
import com.example.e_learning_system.Dto.VideoDtos.VideoDto;
import com.example.e_learning_system.Entities.Module;

import java.util.List;

public class ModuleMapper {


    public static Module fromCreateModuleDtoToModule(CreateModuleDto createModuleDto) {
        if (createModuleDto == null) return null;
        Module module = Module.builder()
                .name(createModuleDto.getModuleName())
                .description(createModuleDto.getModuleDescription())
                .isActive(createModuleDto.isActive())
                .estimatedDuration(createModuleDto.getEstimatedDuration())
                .build();
        return module;
    }

    public static Module fromUpdateModuleDtoToModule(UpdateModuleDto updateModuleDto , Module existingModule ) {
        if (updateModuleDto == null) return null;
        if(existingModule == null) return null;
        if (updateModuleDto.getModuleName() != null) existingModule.setName(updateModuleDto.getModuleName());
        if (updateModuleDto.getModuleDescription() != null) existingModule.setDescription(updateModuleDto.getModuleDescription());
        if(updateModuleDto.getIsActive() != null )existingModule.setActive(updateModuleDto.getIsActive());
        if(updateModuleDto.getEstimatedDuration() != null ) existingModule.setEstimatedDuration(updateModuleDto.getEstimatedDuration());
        return existingModule;
    }


    public static ModuleSummaryDto fromModuletoModuleSummaryDto(Module module ) {
        if (module == null) return null;
        ModuleSummaryDto moduleSummaryDto = ModuleSummaryDto.builder()
                .moduleId(module.getId()+"")
                .moduleName(module.getName())
                .moduleDescription(module.getDescription())
                .estimatedDuration(module.getEstimatedDuration())
                .numberOfvideos(module.getModuleVideos().size())
                .build();
        return moduleSummaryDto;

    }



    public static DetailedModuleDto fromModuleToDetailedModuleDto(Module module) {
        if  (module == null) return null;
        DetailedModuleDto detailedModule = DetailedModuleDto.builder()
                .moduleId(module.getId())
                .moduleName(module.getName())
                .moduleDescription(module.getDescription())
                .estimatedDuration(module.getEstimatedDuration())
                .isActive(module.isActive())
                .build();

        List <VideoDto> videosDto = module.getModuleVideos() == null ? null : VideoMapper.fromModuleVideosToVideoDtos(module.getModuleVideos().stream().toList());

        detailedModule.setVideos(videosDto);
        return detailedModule;

    }




}

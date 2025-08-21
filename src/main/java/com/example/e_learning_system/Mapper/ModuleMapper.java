package com.example.e_learning_system.Mapper;

import com.example.e_learning_system.Dto.ModuleDtos.CreateModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.DetailedModuleDto;
import com.example.e_learning_system.Dto.ModuleDtos.ModuleSummaryDto;
import com.example.e_learning_system.Dto.ModuleDtos.UpdateModuleDto;
import com.example.e_learning_system.Dto.VideoDtos.VideoDto;
import com.example.e_learning_system.Entities.Module;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

public class ModuleMapper {


    public static Module fromCreateModuleDtoToModule(CreateModuleDto createModuleDto) {
        if (createModuleDto == null) return null;
        Module module = new Module();

        module.builder()
                .name(createModuleDto.getModuleName())
                .description(createModuleDto.getModuleDescription())
                .courseStatus(createModuleDto.getCourseStatus())
                .isActive(createModuleDto.isAcitve())
                .estimatedDuration(createModuleDto.getEstimatedDuration())
                .build();
        return module;
    }

    public static Module fromUpdateModuleDtoToModule(UpdateModuleDto updateModuleDto , Module existingModule ) {
        if (updateModuleDto == null) return null;
        if(existingModule == null) return null;

        if (updateModuleDto.getModuleDescription() != null) existingModule.setDescription(updateModuleDto.getModuleDescription());
        if (updateModuleDto.getCourseStatus() != null) existingModule.setCourseStatus(updateModuleDto.getCourseStatus());
        if(updateModuleDto.getModuleDescription()!= null) existingModule.setDescription( updateModuleDto.getModuleDescription());
        if(updateModuleDto.getIsAcitve() != null )existingModule.setActive(updateModuleDto.getIsAcitve());
        if(updateModuleDto.getEstimatedDuration() != null ) existingModule.setEstimatedDuration(updateModuleDto.getEstimatedDuration());
        return existingModule;
    }


    public static ModuleSummaryDto fromModuletoModuleSummaryDto(Module module ) {
        if (module == null) return null;
        ModuleSummaryDto moduleSummaryDto = ModuleSummaryDto.builder()
                .moduleId(module.getId()+"")
                .moduleName(module.getName())
                .moduleDescription(module.getDescription())
                .moduleDescription(module.getDescription())
                .estimateDuratoin(module.getEstimatedDuration())
                .numberOfvideos(module.getModuleVideos().size())
                .build();
        return moduleSummaryDto;

    }



    public static DetailedModuleDto fromModuleToDetailedModuleDto(Module module) {
        if  (module == null) return null;
        DetailedModuleDto detailedModule = (DetailedModuleDto) DetailedModuleDto.builder()
                .moduleId(module.getId())
                .moduleName(module.getName())
                .moduleDescription(module.getDescription())
                .estimatedDuration(module.getEstimatedDuration())
                .isAcitve(module.isActive())
                .courseStatus(module.getCourseStatus())
                .build();

        List <VideoDto> videosDto = module.getModuleVideos() == null ? null : VideoMapper.fromVideoEntitiesToVideoDtos(module.getVideos().stream().toList());

        detailedModule.setVideos(videosDto);
        return detailedModule;

    }




}

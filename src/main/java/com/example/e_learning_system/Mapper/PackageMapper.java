package com.example.e_learning_system.Mapper;

import com.example.e_learning_system.Dto.PackageDtos.PackageRequestDTO;
import com.example.e_learning_system.Dto.PackageDtos.PackageResponseDTO;
import com.example.e_learning_system.Entities.Package;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PackageMapper {

    public Package requestDtoToEntity(PackageRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Package.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .isActive(dto.getIsActive())
                .build();
    }

    public PackageResponseDTO entityToResponseDto(Package entity) {
        if (entity == null) {
            return null;
        }

        return PackageResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PackageResponseDTO entityToResponseDtoWithCourseCount(Package entity, Integer courseCount) {
        if (entity == null) {
            return null;
        }

        PackageResponseDTO dto = entityToResponseDto(entity);
        dto.setTotalCourses(courseCount);
        return dto;
    }

    public void updateEntityFromRequestDto(Package entity, PackageRequestDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setIsActive(dto.getIsActive());
        entity.setUpdatedAt(LocalDateTime.now());
    }
}

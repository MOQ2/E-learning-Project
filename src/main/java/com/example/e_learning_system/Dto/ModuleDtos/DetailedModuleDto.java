package com.example.e_learning_system.Dto.ModuleDtos;

import com.example.e_learning_system.Dto.VideoDtos.VideoDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedModuleDto extends CreateModuleDto {

    @NotNull
    private int moduleId;
    List<VideoDto> videos;
}

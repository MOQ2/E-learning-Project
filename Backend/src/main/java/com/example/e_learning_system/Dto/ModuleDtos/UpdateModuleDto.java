package com.example.e_learning_system.Dto.ModuleDtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateModuleDto  {


    private String moduleName;

    private String moduleDescription;
    private Boolean isActive;

    private Integer estimatedDuration;

}

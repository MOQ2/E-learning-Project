package com.example.e_learning_system.Dto.ModuleDtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleSummaryDto {
    private String moduleId;
    private String moduleName;
    private String moduleDescription;
    private int estimateDuratoin;
    private int numberOfvideos;
    private boolean active;

}


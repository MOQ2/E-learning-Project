package com.example.e_learning_system.Dto.ModuleDtos;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateModuleDto {
    @NotBlank
    @NotEmpty
    @Size(min = 4, max = 200)
    private String moduleName;
    @NotBlank
    @NotEmpty
    @Size(min = 50 )
    private String moduleDescription;
    @NotNull
    private boolean isActive;
    @Min(1)
    private int estimatedDuration;

}

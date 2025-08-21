package com.example.e_learning_system.Dto.ModuleDtos;


import com.example.e_learning_system.Config.CourseStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
    private boolean isAcitve = false;
    @Min(1)
    @NotBlank
    @NotEmpty
    private int estimatedDuration;

    private CourseStatus courseStatus = CourseStatus.DRAFT;

}

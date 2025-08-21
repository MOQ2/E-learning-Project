package com.example.e_learning_system.Dto.ModuleDtos;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCourseDto extends CreateModuleDto {
    @NotNull
    private int id ;

}

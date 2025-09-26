package com.example.e_learning_system.Dto.VideoDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatVideoDto {
    @NotBlank
    private String title;
    private int durationSeconds;
    @NotNull
    private int createdByUserId;
    private String explanation;
    private String whatWeWillLearn;
    private String status;
    private String prerequisites;
}

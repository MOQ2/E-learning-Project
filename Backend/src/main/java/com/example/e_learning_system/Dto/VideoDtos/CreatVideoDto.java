package com.example.e_learning_system.Dto.VideoDtos;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Int;


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
    private Set<Integer> attachments;
    private Integer thumbnail ;
    private int order;

}

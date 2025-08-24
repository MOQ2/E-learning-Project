package com.example.e_learning_system.Dto.VideoDtos;

import com.example.e_learning_system.Dto.AttachmentDtos.CreateAttachmentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAndAddAttachemntToVideoDto extends CreateAttachmentDto {
    private int videoId;
}

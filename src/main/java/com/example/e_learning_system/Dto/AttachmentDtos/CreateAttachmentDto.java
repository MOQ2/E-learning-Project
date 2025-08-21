package com.example.e_learning_system.Dto.AttachmentDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttachmentDto {
    @NotBlank
    @Size(min = 4, max = 100)
    private String title;

    @NotNull
    private MultipartFile file;
}
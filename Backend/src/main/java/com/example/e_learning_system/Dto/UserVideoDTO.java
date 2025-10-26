package com.example.e_learning_system.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserVideoDTO {
    @NotNull(message = "Video ID must not be null")
    private Integer videoId;

}

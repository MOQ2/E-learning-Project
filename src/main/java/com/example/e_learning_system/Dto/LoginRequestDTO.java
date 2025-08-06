package com.example.e_learning_system.Dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
@Setter
@Getter

public class LoginRequestDTO {
    @NotBlank(message = "email should be exist")
    @Email(message = "invalid email format")
    private String email;
    @NotBlank(message = "password should be exist")
    @Size(min = 6 , message = "password should be at least 6 characters")
    private String password;
}

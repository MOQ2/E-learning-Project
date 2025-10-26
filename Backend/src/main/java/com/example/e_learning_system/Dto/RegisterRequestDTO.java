package com.example.e_learning_system.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
@Setter
@Getter
public class RegisterRequestDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8 , message = "password should be at least 8 characters")
    private String password;

    private String bio;
}

package com.example.e_learning_system.Interfaces;

import com.example.e_learning_system.Dto.RegisterRequestDTO;

public interface AuthInterface {

    String login(String username, String password);

    String register(RegisterRequestDTO registerRequestDTO);
}

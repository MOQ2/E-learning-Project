package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.LoginRequestDTO;
import com.example.e_learning_system.Dto.AuthResponseDTO;
import com.example.e_learning_system.Dto.RegisterRequestDTO;
import com.example.e_learning_system.Interfaces.AuthInterface;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthInterface authInterface;

    public  AuthController(AuthInterface authInterface){
        this.authInterface = authInterface;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        try {
            String token = authInterface.register(registerRequestDTO);

            return ResponseEntity.ok(new AuthResponseDTO(token));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new AuthResponseDTO(ex.getMessage()));
        }
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            String token = authInterface.login(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
            return ResponseEntity.ok(new AuthResponseDTO(token));
        }catch (Exception ex){

            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

}

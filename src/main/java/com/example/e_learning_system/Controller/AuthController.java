package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.LoginRequest;
import com.example.e_learning_system.Dto.AuthResponse;
import com.example.e_learning_system.Dto.RegisterRequest;
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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            String token = authInterface.register(registerRequest);

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new AuthResponse(ex.getMessage()));
        }
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String token = authInterface.login(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(new AuthResponse(token));
        }catch (Exception ex){

            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}

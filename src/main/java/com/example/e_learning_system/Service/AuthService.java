package com.example.e_learning_system.Service;

import com.example.e_learning_system.Config.RolesName;
import com.example.e_learning_system.Dto.RegisterRequestDTO;
import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Interfaces.AuthInterface;
import com.example.e_learning_system.Mapper.AuthMapper;
import com.example.e_learning_system.Repository.RolesRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.Security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthInterface {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final RolesRepository rolesRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;



    public String register(RegisterRequestDTO registerRequestDTO){
        RolesEntity defaultRole = rolesRepository.findByName(RolesName.USER)
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        UserEntity userEntity = authMapper.dtoToEntity(registerRequestDTO, defaultRole);
        userEntity.setPassword(bCryptPasswordEncoder.encode(registerRequestDTO.getPassword()));
        userRepository.save(userEntity);

        return jwtUtil.generateToken(userEntity);
    }



    public String login(String email, String password){
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
        UserEntity user = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found"));
    return jwtUtil.generateToken(user);
}

}

package com.example.e_learning_system.Service;

import com.example.e_learning_system.Config.RolesName;
import com.example.e_learning_system.Dto.RegisterRequestDTO;
import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Mapper.AuthMapper;
import com.example.e_learning_system.Repository.RolesRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.Security.JwtUtil;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import com.example.e_learning_system.excpetions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private RolesRepository rolesRepository;
    @Mock private AuthMapper authMapper;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private BCryptPasswordEncoder bCryptPasswordEncoder;

    private RegisterRequestDTO registerDTO;
    private UserEntity userEntity;
    private RolesEntity defaultRole;

    @BeforeEach
    void setUp() {
        registerDTO = new RegisterRequestDTO();
        registerDTO.setEmail("test@example.com");
        registerDTO.setPassword("password");

        defaultRole = new RolesEntity();
        defaultRole.setName(RolesName.USER);

        userEntity = new UserEntity();
        userEntity.setEmail(registerDTO.getEmail());
        userEntity.setPassword("hashedPassword");
    }


    @Test
    @DisplayName("Should throw ValidationException if user email already exists")
    void register_ShouldThrowValidationException_WhenEmailExists() {
        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.of(userEntity));

        assertThatThrownBy(() -> authService.register(registerDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFound if default role not found")
    void register_ShouldThrowResourceNotFound_WhenRoleMissing() {
        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.empty());
        when(rolesRepository.findByName(RolesName.USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerDTO))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining(RolesName.USER.name());

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should register user successfully and return JWT")
    void register_ShouldRegisterSuccessfully() {
        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.empty());
        when(rolesRepository.findByName(RolesName.USER)).thenReturn(Optional.of(defaultRole));
        when(authMapper.dtoToEntity(registerDTO, defaultRole)).thenReturn(userEntity);
        when(bCryptPasswordEncoder.encode(registerDTO.getPassword())).thenReturn("hashedPassword");
        when(jwtUtil.generateToken(userEntity)).thenReturn("jwt-token");

        String token = authService.register(registerDTO);

        assertThat(token).isEqualTo("jwt-token");
        verify(userRepository).save(userEntity);
        assertThat(userEntity.getPassword()).isEqualTo("hashedPassword");
    }


    @Test
    @DisplayName("Should login successfully and update lastLoginAt")
    void login_ShouldLoginSuccessfully() {
        String email = "test@example.com";
        String password = "password";

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(jwtUtil.generateToken(userEntity)).thenReturn("jwt-token");

        String token = authService.login(email, password);

        assertThat(token).isEqualTo("jwt-token");
        assertThat(userEntity.getLastLoginAt()).isNotNull();
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFound if email not found during login")
    void login_ShouldThrowResourceNotFound_WhenEmailNotFound() {
        String email = "notfound@example.com";
        String password = "password";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining(email);

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw AuthenticationServiceException if password is wrong")
    void login_ShouldThrowAuthenticationException_WhenPasswordWrong() {
        String email = "test@example.com";
        String password = "wrongpassword";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationServiceException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessageContaining("Bad credentials");

        verify(userRepository, never()).save(any(UserEntity.class));
    }
}

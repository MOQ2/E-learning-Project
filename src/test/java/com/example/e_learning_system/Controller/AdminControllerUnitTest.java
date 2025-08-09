package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Dto.RoleUpdateDTO;
import com.example.e_learning_system.Entities.PermissionsEntity;
import com.example.e_learning_system.Service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController - Unit Tests")
class AdminControllerUnitTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should get all roles successfully")
    void shouldGetAllRolesSuccessfully() throws Exception {
        // Given
        when(adminService.viewRoles()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isOk());

        verify(adminService, times(1)).viewRoles();
    }

    @Test
    @DisplayName("Should create new role successfully")
    void shouldCreateNewRoleSuccessfully() throws Exception {
        // Given
        RoleUpdateDTO roleDTO = new RoleUpdateDTO();
        roleDTO.setRole("TEACHER");
        
        doNothing().when(adminService).createRole(any(RoleUpdateDTO.class));

        // When & Then
        mockMvc.perform(post("/api/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk());

        verify(adminService, times(1)).createRole(any(RoleUpdateDTO.class));
    }

    @Test
    @DisplayName("Should delete role successfully")
    void shouldDeleteRoleSuccessfully() throws Exception {
        // Given
        doNothing().when(adminService).removeRole(anyInt());

        // When & Then
        mockMvc.perform(delete("/api/admin/roles/{roleId}", 1))
                .andExpect(status().isOk());

        verify(adminService, times(1)).removeRole(1);
    }

    @Test
    @DisplayName("Should update role successfully")
    void shouldUpdateRoleSuccessfully() throws Exception {
        // Given
        RoleUpdateDTO roleDTO = new RoleUpdateDTO();
        roleDTO.setRole("UPDATED_ROLE");
        
        doNothing().when(adminService).updateRole(any(RoleUpdateDTO.class));

        // When & Then
        mockMvc.perform(put("/api/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk());

        verify(adminService, times(1)).updateRole(any(RoleUpdateDTO.class));
    }

    @Test
    @DisplayName("Should get all permissions successfully")
    void shouldGetAllPermissionsSuccessfully() throws Exception {
        // Given
        when(adminService.viewPermissions()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/admin/permissions"))
                .andExpect(status().isOk());

        verify(adminService, times(1)).viewPermissions();
    }

    @Test
    @DisplayName("Should get all users successfully")
    void shouldGetAllUsersSuccessfully() throws Exception {
        // Given
        when(adminService.getUsers()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());

        verify(adminService, times(1)).getUsers();
    }

    @Test
    @DisplayName("Should add permission to role successfully")
    void shouldAddPermissionToRoleSuccessfully() throws Exception {
        // Given
        PermissionsEntity permission = new PermissionsEntity();
        permission.setId(1);
        permission.setName("user:read");

        doNothing().when(adminService).addPermissionToRole(anyInt(), any(PermissionsEntity.class));

        // When & Then
        mockMvc.perform(post("/api/admin/roles/{roleId}/permissions", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(permission)))
                .andExpect(status().isOk());

        verify(adminService, times(1)).addPermissionToRole(eq(1), any(PermissionsEntity.class));
    }

    @Test
    @DisplayName("Should remove permission from role successfully")
    void shouldRemovePermissionFromRoleSuccessfully() throws Exception {
        // Given
        doNothing().when(adminService).removePermissionFromRole(anyInt(), anyInt());

        // When & Then
        mockMvc.perform(delete("/api/admin/roles/{roleId}/permissions/{permissionId}", 1, 1))
                .andExpect(status().isOk());

        verify(adminService, times(1)).removePermissionFromRole(1, 1);
    }
}

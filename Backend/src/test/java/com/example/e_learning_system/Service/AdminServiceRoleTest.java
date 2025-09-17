package com.example.e_learning_system.Service;

import com.example.e_learning_system.Config.RolesName;
import com.example.e_learning_system.Dto.RoleResponseDTO;
import com.example.e_learning_system.Dto.RoleUpdateDTO;
import com.example.e_learning_system.Entities.PermissionsEntity;
import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Repository.PermissionsEntityRepository;
import com.example.e_learning_system.Repository.RolesRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService - Role Management Tests")
class AdminServiceRoleTest {

    @Mock
    private RolesRepository rolesRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionsEntityRepository permissionsEntityRepository;

    @InjectMocks
    private AdminService adminService;

    private RolesEntity testRole;
    private PermissionsEntity testPermission;

    @BeforeEach
    void setUp() {
        // Setup test data
        testRole = new RolesEntity();
        testRole.setId(1);
        testRole.setName(RolesName.ADMIN);
        testRole.setPermissions(new java.util.HashSet<>());

        testPermission = new PermissionsEntity();
        testPermission.setId(1);
        testPermission.setName("user:read");
        testPermission.setDescription("Read user information");

        testRole.getPermissions().add(testPermission);

    }

    // ==================== VIEW ROLES TESTS ====================

    @Test
    @DisplayName("Should return all roles successfully")
    void shouldReturnAllRolesSuccessfully() {
        // Given
        RolesEntity role1 = new RolesEntity();
        role1.setId(1);
        role1.setName(RolesName.ADMIN);

        RolesEntity role2 = new RolesEntity();
        role2.setId(2);
        role2.setName(RolesName.USER);

        List<RolesEntity> mockRoles = Arrays.asList(role1, role2);

        when(rolesRepository.findAll()).thenReturn(mockRoles);

        // When
        List<RoleResponseDTO> result = adminService.viewRoles();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(1).getId()).isEqualTo(2);
        verify(rolesRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no roles exist")
    void shouldReturnEmptyListWhenNoRolesExist() {
        // Given
        when(rolesRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<RoleResponseDTO> result = adminService.viewRoles();

        // Then
        assertThat(result).isEmpty();
        verify(rolesRepository, times(1)).findAll();
    }

    // ==================== CREATE ROLE TESTS ====================

    @Test
    @DisplayName("Should create new role successfully")
    void shouldCreateNewRoleSuccessfully() {
        // Given
        RoleUpdateDTO roleUpdateDTO = new RoleUpdateDTO();
        roleUpdateDTO.setRole("TEACHER");

        RolesEntity savedRole = new RolesEntity();
        savedRole.setId(3);
        savedRole.setName(RolesName.TEACHER);

        when(rolesRepository.save(any(RolesEntity.class))).thenReturn(savedRole);

        // When
        adminService.createRole(roleUpdateDTO);

        // Then
        verify(rolesRepository, times(1)).save(argThat(role -> 
            role.getName() == RolesName.TEACHER
        ));
    }

    @Test
    @DisplayName("Should handle case insensitive role creation")
    void shouldHandleCaseInsensitiveRoleCreation() {
        // Given
        RoleUpdateDTO roleUpdateDTO = new RoleUpdateDTO();
        roleUpdateDTO.setRole("teacher"); // lowercase

        when(rolesRepository.save(any(RolesEntity.class))).thenReturn(testRole);

        // When
        adminService.createRole(roleUpdateDTO);

        // Then
        verify(rolesRepository, times(1)).save(argThat(role -> 
            role.getName() == RolesName.TEACHER
        ));
    }

    @Test
    @DisplayName("Should throw exception when creating role with invalid name")
    void shouldThrowExceptionWhenCreatingRoleWithInvalidName() {
        // Given
        RoleUpdateDTO roleUpdateDTO = new RoleUpdateDTO();
        roleUpdateDTO.setRole("INVALID_ROLE");

        // When & Then
        assertThatThrownBy(() -> adminService.createRole(roleUpdateDTO))
            .isInstanceOf(IllegalArgumentException.class);

        verify(rolesRepository, never()).save(any(RolesEntity.class));
    }

    // ==================== UPDATE ROLE TESTS ====================

    @Test
    @DisplayName("Should update existing role successfully")
    void shouldUpdateExistingRoleSuccessfully() {
        // Given
        int roleId = 1;
        RoleUpdateDTO roleUpdateDTO = new RoleUpdateDTO();
        roleUpdateDTO.setId(roleId);
        roleUpdateDTO.setRole("USER");

        RolesEntity existingRole = new RolesEntity();
        existingRole.setId(roleId);
        existingRole.setName(RolesName.ADMIN);

        when(rolesRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(rolesRepository.save(any(RolesEntity.class))).thenReturn(existingRole);

        // When
        adminService.updateRole(roleUpdateDTO);

        // Then
        verify(rolesRepository, times(1)).findById(roleId);
        verify(rolesRepository, times(1)).save(argThat(role -> 
            role.getName() == RolesName.USER && role.getId() == roleId
        ));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent role")
    void shouldThrowExceptionWhenUpdatingNonExistentRole() {
        // Given
        int roleId = 999;
        RoleUpdateDTO roleUpdateDTO = new RoleUpdateDTO();
        roleUpdateDTO.setId(roleId);
        roleUpdateDTO.setRole("USER");

        when(rolesRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminService.updateRole(roleUpdateDTO))
            .isInstanceOf(ResourceNotFound.class)
            .hasMessageContaining("Role");

        verify(rolesRepository, times(1)).findById(roleId);
        verify(rolesRepository, never()).save(any(RolesEntity.class));
    }

    // ==================== DELETE ROLE TESTS ====================

    @Test
    @DisplayName("Should delete role successfully")
    void shouldDeleteRoleSuccessfully() {
        // Given
        int roleId = 1;
        doNothing().when(rolesRepository).deleteById(roleId);

        // When
        adminService.removeRole(roleId);

        // Then
        verify(rolesRepository, times(1)).deleteById(roleId);
    }

    // ==================== ADD PERMISSION TO ROLE TESTS ====================

    @Test
    @DisplayName("Should add permission to role successfully")
    void shouldAddPermissionToRoleSuccessfully() {
        // Given
        int roleId = 1;
        when(rolesRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(rolesRepository.save(any(RolesEntity.class))).thenReturn(testRole);

        // When
        adminService.addPermissionToRole(roleId, testPermission);

        // Then
        verify(rolesRepository, times(1)).findById(roleId);
        verify(rolesRepository, times(1)).save(argThat(role -> 
            role.getPermissions().contains(testPermission) && role.getId() == roleId
        ));

    }

    @Test
    @DisplayName("Should throw exception when adding permission to non-existent role")
    void shouldThrowExceptionWhenAddingPermissionToNonExistentRole() {
        // Given
        int roleId = 999;
        when(rolesRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminService.addPermissionToRole(roleId, testPermission))
            .isInstanceOf(ResourceNotFound.class)
            .hasMessageContaining("Role");

        verify(rolesRepository, times(1)).findById(roleId);
        
    }

    // ==================== REMOVE PERMISSION FROM ROLE TESTS ====================

    @Test
    @DisplayName("Should remove permission from role successfully")
    void shouldRemovePermissionFromRoleSuccessfully() {
        // Given
        int roleId = 1;
        int permissionId = 1;

        when(rolesRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(permissionsEntityRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));
        when(rolesRepository.save(any(RolesEntity.class))).thenReturn(testRole);

        // When
        adminService.removePermissionFromRole(roleId, permissionId);

        // Then
        verify(rolesRepository, times(1)).findById(roleId);
        verify(permissionsEntityRepository, times(1)).findById(permissionId);
        verify(rolesRepository, times(1)).save(testRole);
    }

    @Test
    @DisplayName("Should throw exception when removing permission from non-existent role")
    void shouldThrowExceptionWhenRemovingPermissionFromNonExistentRole() {
        // Given
        int roleId = 999;
        int permissionId = 1;

        when(rolesRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminService.removePermissionFromRole(roleId, permissionId))
            .isInstanceOf(ResourceNotFound.class)
            .hasMessageContaining("Role");

        verify(rolesRepository, times(1)).findById(roleId);
        verify(permissionsEntityRepository, never()).findById(anyInt());
        verify(rolesRepository, never()).save(any(RolesEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent permission from role")
    void shouldThrowExceptionWhenRemovingNonExistentPermissionFromRole() {
        // Given
        int roleId = 1;
        int permissionId = 999;

        when(rolesRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(permissionsEntityRepository.findById(permissionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminService.removePermissionFromRole(roleId, permissionId))
            .isInstanceOf(ResourceNotFound.class)
            .hasMessageContaining("Permission");

        verify(rolesRepository, times(1)).findById(roleId);
        verify(permissionsEntityRepository, times(1)).findById(permissionId);
        verify(rolesRepository, never()).save(any(RolesEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when role-permission relationship does not exist")
    void shouldThrowExceptionWhenRolePermissionRelationshipDoesNotExist() {
        // Given
        int roleId = 1;
        int permissionId = 2; // Different permission ID that's not in testRole
        
        PermissionsEntity differentPermission = new PermissionsEntity();
        differentPermission.setId(2);
        differentPermission.setName("different:permission");
        differentPermission.setDescription("A different permission");

        when(rolesRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(permissionsEntityRepository.findById(permissionId)).thenReturn(Optional.of(differentPermission));

        // When & Then
        assertThatThrownBy(() -> adminService.removePermissionFromRole(roleId, permissionId))
            .isInstanceOf(ResourceNotFound.class)
            .hasMessageContaining("Role-Permission relationship");

        verify(rolesRepository, times(1)).findById(roleId);
        verify(permissionsEntityRepository, times(1)).findById(permissionId);
        verify(rolesRepository, times(1)).save(testRole);
    }

    // ==================== EDGE CASES AND ERROR HANDLING ====================

    @Test
    @DisplayName("Should handle null role update DTO")
    void shouldHandleNullRoleUpdateDTO() {
        // When & Then
        assertThatThrownBy(() -> adminService.createRole(null))
            .isInstanceOf(NullPointerException.class);

        verify(rolesRepository, never()).save(any(RolesEntity.class));
    }

    @Test
    @DisplayName("Should handle null permission when adding to role")
    void shouldHandleNullPermissionWhenAddingToRole() {
        // Given
        int roleId = 1;
        // No need to mock repository since validation happens first

        // When & Then
        assertThatThrownBy(() -> adminService.addPermissionToRole(roleId, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Permission cannot be null");

        // Verify no repository interactions since validation fails first
        verify(rolesRepository, never()).findById(anyInt());
        verify(rolesRepository, never()).save(any(RolesEntity.class));
    }
}

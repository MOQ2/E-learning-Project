package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Config.RolesName;
import com.example.e_learning_system.Dto.*;
import com.example.e_learning_system.Entities.PermissionsEntity;
import com.example.e_learning_system.Service.AdminService;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Get all roles with permissions
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDTO>> getAllRoles() {

        return ResponseEntity.ok(adminService.viewRoles());
    }

    // Get all permissions
    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionsResponsDTO>> getAllPermissions() {
        return ResponseEntity.ok(adminService.viewPermissions());
    }

    // Get all users
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getUsers());
    }

    // Update existing user
    @PutMapping("/users")
    public ResponseEntity<Void> updateUser(@RequestBody AdminUserUpdateDTO userDto) {
        adminService.updateUser(userDto);
        return ResponseEntity.ok().build();
    }

    // Update existing role
    @PutMapping("/roles")
    public ResponseEntity<Void> updateRole(@RequestBody RoleUpdateDTO roleDto) {
        adminService.updateRole(roleDto);
        return ResponseEntity.ok().build();
    }

    // Create new role
    @PostMapping("/roles")
    public ResponseEntity<Void> createRole(@RequestBody RoleUpdateDTO roleDto) {
        adminService.createRole(roleDto);
        return ResponseEntity.ok().build();
    }

    // Remove role by id
    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<String> deleteRole(@PathVariable int roleId) {
        adminService.removeRole(roleId);
        return ResponseEntity.ok("removed Successful ");
    }

    // Add permission to role
    @PostMapping("/roles/{roleId}/permissions")
    public ResponseEntity<Void> addPermissionToRole(
            @PathVariable int roleId,
            @RequestBody PermissionsEntity permission) {
        adminService.addPermissionToRole(roleId, permission);
        return ResponseEntity.ok().build();
    }

    // Remove permission from role
    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    public ResponseEntity<String> removePermissionFromRole(
            @PathVariable int roleId,
            @PathVariable int permissionId) {
        try {
            adminService.removePermissionFromRole(roleId, permissionId);
            return ResponseEntity.ok("Permission removed successfully");
        } catch (ResourceNotFound e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to remove permission: " + e.getMessage());
        }
    }
}

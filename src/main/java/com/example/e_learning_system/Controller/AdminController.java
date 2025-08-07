package com.example.e_learning_system.Controller;

import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequestMapping("/api/admin/")
@RestController
public class AdminController {
    @Autowired
    AdminService adminService;

    @GetMapping("user/{id}/role")
    public ResponseEntity<RolesEntity> getRoles(@PathVariable String id){
        RolesEntity role = adminService.getUserRole(Integer.parseInt(id));
        return ResponseEntity.ok(role);
    }

    @PutMapping("/user/{id}/role")
    public ResponseEntity<RolesEntity> addRoles(@PathVariable String id, @RequestBody RolesEntity role){
        role.setUpdatedAt(LocalDateTime.now());

        adminService.updateRole(Integer.parseInt(id),role);
        return ResponseEntity.ok(role);
    }



}

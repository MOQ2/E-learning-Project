package com.example.e_learning_system.Service;

import com.example.e_learning_system.Config.RolesName;
import com.example.e_learning_system.Dto.*;
import com.example.e_learning_system.Entities.PermissionsEntity;
import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Entities.RolesPermissionsEntity;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Repository.PermissionsEntityRepository;
import com.example.e_learning_system.Repository.RolePermissionRepository;
import com.example.e_learning_system.Repository.RolesRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@Transactional
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private RolesRepository rolesRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PermissionsEntityRepository permissionsEntityRepository;
    @Autowired
    private RolePermissionRepository rolePermissionRepository;





    /*
    * role managment :
    *   1- create/update a role
    *   2- assign permessions to a role
    *   3- remove permission from a role ;
    *   4- update a user role
    *
    * */

    ///  view available roles in the system with their permissions
    public List<RoleResponseDTO> viewRoles()
    {
        List <RolesEntity> roles = rolesRepository.findAll();
        System.out.print(roles);
        return roles.stream().map(RoleResponseDTO::new).toList();
    }

    ///  view available permissions in the system
    public List<PermissionsResponsDTO> viewPermissions (){
        List<PermissionsEntity> permissions =permissionsEntityRepository.findAll();
        return  permissions.stream().map(PermissionsResponsDTO::new).toList();

    }
    /// view all the users in the system with their roles and info
    public List<UserResponseDTO> getUsers() {
        return userRepository.findAll().stream().map(UserResponseDTO::new).toList();
    }

    /// update an existing User
    public void updateUser (AdminUserUpdateDTO  adminUserUpdateDTO) {
        UserEntity userEntity = userRepository.findById(adminUserUpdateDTO.getId()).orElseThrow(()->ResourceNotFound.userNotFound(adminUserUpdateDTO.getId()+""));
        userEntity.setName(adminUserUpdateDTO.getName());
        userEntity.setEmail(adminUserUpdateDTO.getEmail());
        userEntity.setPhone(adminUserUpdateDTO.getPhone());
        rolesRepository.findByName(RolesName.valueOf(adminUserUpdateDTO.getRole())).ifPresent(userEntity::setRole);
        userRepository.save(userEntity);
    }

    ///  update an existing role
    public void updateRole(RoleUpdateDTO roleUpdateDTO) {
        RolesEntity existingRole = rolesRepository.findById(roleUpdateDTO.getId())
                .orElseThrow(() -> ResourceNotFound.roleNotFound(roleUpdateDTO.getId() + ""));

        existingRole.setName(RolesName.valueOf(roleUpdateDTO.getRole().toUpperCase()));
        rolesRepository.save(existingRole);
    }

    ///  create a new Role
    public  void createRole(RoleUpdateDTO roleUpdateDTO) {
        RolesEntity rolesEntity = new RolesEntity();
        rolesEntity.setName(RolesName.valueOf(roleUpdateDTO.getRole().toUpperCase())) ;
        rolesRepository.save(rolesEntity);
    }


    ///  remove a role
    public void removeRole(int role_id){
        rolesRepository.deleteById(role_id);
    }

    public void addPermissionToRole(int role_id, PermissionsEntity permissionsEntity){
        if (permissionsEntity == null) {
            throw new NullPointerException("Permission cannot be null");
        }
        RolesEntity role =rolesRepository.findById(role_id).orElseThrow(()->ResourceNotFound.roleNotFound(role_id+""));
        RolesPermissionsEntity  rolesPermissionsEntity = new RolesPermissionsEntity();
        rolesPermissionsEntity.setRole(role);
        rolesPermissionsEntity.setPermission(permissionsEntity);
        rolePermissionRepository.save(rolesPermissionsEntity);

    }
    @Transactional
    public void removePermissionFromRole(int roleId, int permissionId) {
        logger.info("Attempting to remove permission {} from role {}", permissionId, roleId);
        
        // Validate that role exists
        rolesRepository.findById(roleId)
                .orElseThrow(() -> ResourceNotFound.roleNotFound(roleId + ""));
        logger.debug("Role {} exists", roleId);
        
        // Validate that permission exists
        permissionsEntityRepository.findById(permissionId)
                .orElseThrow(() -> ResourceNotFound.permissionNotFound(permissionId + ""));
        logger.debug("Permission {} exists", permissionId);
        
        // Try to delete using JPQL query first (more reliable)
        int deletedRows = rolePermissionRepository.deleteByRoleIdAndPermissionIdJPQL(roleId, permissionId);
        logger.info("Deleted {} rows for roleId: {} and permissionId: {} using JPQL", deletedRows, roleId, permissionId);
        
        if (deletedRows == 0) {
            logger.warn("No role-permission relationship found for roleId: {} and permissionId: {}", roleId, permissionId);
            throw ResourceNotFound.rolePermissionNotFound(roleId + "", permissionId + "");
        }
        
        logger.info("Successfully removed permission {} from role {}", permissionId, roleId);
    }


}

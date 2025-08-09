package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.RolesPermissionsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolesPermissionsEntity, Integer> {
    
    public RolesPermissionsEntity findRolesPermissionsEntitiesById(int id);
    
    public Optional<RolesPermissionsEntity> findByRoleIdAndPermissionId(int roleId, int permissionId);
    
    @Query("SELECT rpe FROM RolesPermissionsEntity rpe JOIN FETCH rpe.role JOIN FETCH rpe.permission WHERE rpe.role.id = :roleId AND rpe.permission.id = :permissionId")
    public Optional<RolesPermissionsEntity> findByRoleIdAndPermissionIdWithFetch(@Param("roleId") int roleId, @Param("permissionId") int permissionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "DELETE FROM role_permissions WHERE role_id = :roleId AND permission_id = :permissionId",
            nativeQuery = true
    )
    public int deleteByRoleIdAndPermissionIdNative(@Param("roleId") int roleId, @Param("permissionId") int permissionId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RolesPermissionsEntity rpe WHERE rpe.role.id = :roleId AND rpe.permission.id = :permissionId")
    public int deleteByRoleIdAndPermissionIdJPQL(@Param("roleId") int roleId, @Param("permissionId") int permissionId);
    
    public int deleteByRoleIdAndPermissionId(int roleId, int permissionId);
}

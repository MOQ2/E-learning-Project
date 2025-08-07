package com.example.e_learning_system.Service;

import com.example.e_learning_system.Config.RolesName;
import com.example.e_learning_system.Entities.RolesEntity;
import com.example.e_learning_system.Entities.UserEntity;
import com.example.e_learning_system.Repository.RolesRepository;
import com.example.e_learning_system.Repository.UserRepository;
import com.example.e_learning_system.excpetions.ResourceNotFound;
import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class AdminService {

    @Autowired
    private RolesRepository rolesRepository;
    @Autowired
    private UserRepository userRepository;


    public Page<UserEntity> viewUsers(int page , int size , String sortBy, String order) {
        Sort sort = order.equalsIgnoreCase("decs")?Sort.by(sortBy).descending():Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page , size , sort);
        return userRepository.findAll(pageable);

    }


    public List<RolesEntity> viewRoles(){
        return rolesRepository.findAll();
    }
    public void updateRole(int id, RolesEntity role) {
        RolesEntity existingRole = rolesRepository.findById(id)
                .orElseThrow(() -> ResourceNotFound.roleNotFound(id + ""));

        role.setId(id);

    }

    void createRole(RolesName rolesName) {
        RolesEntity rolesEntity = new RolesEntity();
        rolesEntity.setName(rolesName);
    }
    public void updateUserRole(int  user_id , RolesName role){
        RolesEntity existingRole = rolesRepository.findByName(role)
                .orElseThrow(() -> ResourceNotFound.roleNotFound(role.name()));
        UserEntity user= userRepository.findById(user_id).orElseThrow(() -> ResourceNotFound.userNotFound(String.valueOf(user_id)));
        user.setRole(existingRole);

    }
    public void removeRole(int role_id){
        rolesRepository.deleteById(role_id);
    }

    public RolesEntity getUserRole(int user_id){
        UserEntity user = userRepository.findById(user_id).orElseThrow(() -> ResourceNotFound.userNotFound(String.valueOf(user_id)));
        RolesEntity role =user.getRole();
        rolesRepository.findById(role.getId()).orElseThrow(() -> ResourceNotFound.roleNotFound(String.valueOf(role.getId())));

        return  role;
    }



}

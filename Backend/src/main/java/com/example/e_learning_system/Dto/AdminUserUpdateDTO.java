package com.example.e_learning_system.Dto;

import com.example.e_learning_system.Entities.UserEntity;
import lombok.*;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateDTO {
    private String name ;
    private String role;
    private String email;
    private String phone;
    private int id ;
    private boolean isActive;
    public AdminUserUpdateDTO (UserEntity user) {
        this.id = user.getId();
        this.name = user.getName();
        this.role = user.getRole().getName().name();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.isActive = user.getIsActive();
    }

}

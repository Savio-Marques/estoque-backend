package com.marques.estoque.dto;

import com.marques.estoque.model.user.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserCreateDTO {
    private Long id;
    private String name;
    private String email;
    private String password;
    private UserRole role;
}

package com.marques.estoque.dto;

import com.marques.estoque.model.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserCreateDTO {
    private Long id;

    @NotBlank(message = "O nome do usuário não pode ser vazio.")
    private String name;

    @NotBlank(message = "O username do usuário não pode ser vazio.")
    private String username;

    private String password;
    private UserRole role;
}

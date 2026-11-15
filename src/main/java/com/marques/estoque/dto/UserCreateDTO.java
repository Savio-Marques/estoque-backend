package com.marques.estoque.dto;

import com.marques.estoque.model.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserCreateDTO {
    private Long id;

    @NotBlank(message = "O nome do usuário não pode ser vazio.")
    @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres.")
    private String name;

    @NotBlank(message = "O username do usuário não pode ser vazio.")
    @Size(max = 255, message = "O username deve ter no máximo 255 caracteres.")
    private String username;

    @Size(max = 255, message = "A senha deve ter no máximo 255 caracteres.")
    private String password;
    private UserRole role;
}

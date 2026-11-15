package com.marques.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank(message = "O username do usuário não pode ser vazio.")
    private String username;

    @NotBlank(message = "A senha do usuário não pode ser vazia.")
    private String password;
}

package com.marques.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank(message = "O username do usuário não pode ser vazio.")
    @Size(max = 255, message = "O username deve ter no máximo 255 caracteres.")
    private String username;

    @NotBlank(message = "A senha do usuário não pode ser vazia.")
    @Size(max = 255, message = "A senha deve ter no máximo 255 caracteres.")
    private String password;
}

package com.marques.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateDTO {

    @NotBlank(message = "O nome não pode ser vazio.")
    @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres.")
    private String name;

    @Size(max = 255, message = "A senha atual deve ter no máximo 255 caracteres.")
    private String currentPassword;

    @Size(max = 255, message = "A nova senha deve ter no máximo 255 caracteres.")
    private String newPassword;
}

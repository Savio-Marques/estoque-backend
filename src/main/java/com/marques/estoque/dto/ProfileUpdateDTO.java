package com.marques.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateDTO {

    @NotBlank(message = "O nome não pode ser vazio.")
    private String name;

    private String currentPassword;

    private String newPassword;
}

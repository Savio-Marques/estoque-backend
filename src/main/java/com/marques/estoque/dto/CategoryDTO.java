package com.marques.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "O nome da categoria não pode ser vazio.")
    @Size(max = 255, message = "O nome da categoria deve ter no máximo 255 caracteres.")
    private String name;

    private List<ProductDTO> products;
}

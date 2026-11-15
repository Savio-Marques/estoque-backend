package com.marques.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ProductDTO {

    private Long id;

    @NotBlank(message = "O nome do produto não pode ser vazio.")
    private String name;

    @NotNull(message = "A quantidade não pode ser nula.")
    @PositiveOrZero(message = "A quantidade não pode ser um número negativo.")
    private Integer qtd;

    @NotNull(message = "O produto precisa pertencer a uma categoria.")
    private Long categoryId;

    private String categoryName;

    private String status;
}

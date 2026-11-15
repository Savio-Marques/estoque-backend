package com.marques.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ProductDTO {

    private Long id;

    @NotBlank(message = "O nome do produto não pode ser vazio.")
    @Size(max = 255, message = "O nome do produto deve ter no máximo 255 caracteres.")
    private String name;

    @NotNull(message = "A quantidade não pode ser nula.")
    @PositiveOrZero(message = "A quantidade não pode ser um número negativo.")
    private Integer qtd;

    @PositiveOrZero(message = "O preço de custo não pode ser negativo.")
    private BigDecimal costPrice;

    @PositiveOrZero(message = "O preço de venda não pode ser negativo.")
    private BigDecimal salePrice;

    @PositiveOrZero(message = "O estoque mínimo não pode ser negativo.")
    private Integer minQuantity;

    @NotNull(message = "O produto precisa pertencer a uma categoria.")
    private Long categoryId;

    private String categoryName;

    private String status;
}

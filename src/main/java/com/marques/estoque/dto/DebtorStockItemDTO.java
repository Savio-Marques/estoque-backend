package com.marques.estoque.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DebtorStockItemDTO {

    @NotNull(message = "O ID do produto é obrigatório.")
    private Long productId;

    @NotNull(message = "A quantidade do item é obrigatória.")
    @Positive(message = "A quantidade do item deve ser maior que zero.")
    private Integer quantity;
}

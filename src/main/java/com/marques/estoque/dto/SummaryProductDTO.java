package com.marques.estoque.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class SummaryProductDTO {

    private Long totalProduct;
    private Long lowStock;
    private Long noStock;

}

package com.marques.estoque.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class SummaryProductDTO {

    private Integer totalProduct;
    private Integer lowStock;
    private Integer noStock;

}

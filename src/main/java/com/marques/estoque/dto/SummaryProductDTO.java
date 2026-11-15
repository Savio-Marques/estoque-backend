package com.marques.estoque.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter @Setter
public class SummaryProductDTO {

    private Integer totalProduct;
    private Integer lowStock;
    private Integer noStock;
    private BigDecimal totalStockValue;
    private BigDecimal totalSaleValue;

}

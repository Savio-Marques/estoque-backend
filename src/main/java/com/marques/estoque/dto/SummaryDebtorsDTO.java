package com.marques.estoque.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter@Setter
public class SummaryDebtorsDTO {
    private BigDecimal totalValue;
    private Integer totalDebtors;
}

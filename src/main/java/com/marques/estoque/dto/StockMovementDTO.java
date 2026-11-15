package com.marques.estoque.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class StockMovementDTO {
    private Long id;
    private String productName;
    private String movementType;
    private Integer previousQuantity;
    private Integer newQuantity;
    private Integer quantityDelta;
    private LocalDateTime createdAt;
}

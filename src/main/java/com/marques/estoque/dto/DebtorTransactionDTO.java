package com.marques.estoque.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class DebtorTransactionDTO {
    private Long id;
    private Long debtorId;
    private String debtorName;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private String description;
    private LocalDateTime createdAt;
}

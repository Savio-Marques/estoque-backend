package com.marques.estoque.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PartialPaymentDTO {

    @NotNull(message = "O valor do pagamento não pode ser nulo.")
    @Positive(message = "O valor do pagamento deve ser maior que zero.")
    private BigDecimal amount;
}

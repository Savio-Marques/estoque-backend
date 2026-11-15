package com.marques.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class DebtorDTO {

    private Long id;

    @NotBlank(message = "O nome do devedor não pode ser vazio.")
    private String name;

    private String description;

    @NotNull(message = "O valor devido não pode ser nulo.")
    @PositiveOrZero(message = "O valor devido não pode ser um número negativo.")
    private BigDecimal value;

    private LocalDateTime date;

}

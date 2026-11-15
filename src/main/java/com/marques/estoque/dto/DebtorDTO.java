package com.marques.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class DebtorDTO {

    private Long id;

    @NotBlank(message = "O nome do devedor não pode ser vazio.")
    @Size(max = 255, message = "O nome do devedor deve ter no máximo 255 caracteres.")
    private String name;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
    private String description;

    @NotNull(message = "O valor devido não pode ser nulo.")
    @PositiveOrZero(message = "O valor devido não pode ser um número negativo.")
    private BigDecimal value;

    private LocalDateTime date;

}

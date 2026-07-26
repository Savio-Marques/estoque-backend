package com.marques.estoque.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class DebtorDTO {

    private Long id;

    private String name;

    private String description;

    private BigDecimal value;

    private LocalDateTime date;

}

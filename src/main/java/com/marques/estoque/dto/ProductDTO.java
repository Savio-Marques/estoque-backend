package com.marques.estoque.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductDTO {

    private Long id;

    private String name;

    private Integer qtd;

    private Long categoryId;
}

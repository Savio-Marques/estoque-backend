package com.marques.estoque.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ProductDTO {

    private Long id;

    private String name;

    private Integer qtd;

    private Long categoryId;

    private String categoryName;

    private String status;
}

package com.marques.estoque.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserResponseDTO {
    private Long id;
    private String name;
    private String username;
    private String role;
}

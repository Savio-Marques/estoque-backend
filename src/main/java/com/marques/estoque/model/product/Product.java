package com.marques.estoque.model.product;

import com.marques.estoque.model.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "tb_produto")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "O nome do produto não pode ser vazio.")
    private String name;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "A quantidade não pode ser nula.")
    @PositiveOrZero(message = "A quantidade não pode ser um número negativo.")
    private Integer qtd;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @NotNull(message = "O produto precisa pertencer a uma categoria.")
    private Category categories;
}

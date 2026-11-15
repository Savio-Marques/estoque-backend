package com.marques.estoque.model.product;

import com.marques.estoque.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

    @Column(name = "cost_price", nullable = false)
    @PositiveOrZero(message = "O preço de custo não pode ser negativo.")
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "sale_price", nullable = false)
    @PositiveOrZero(message = "O preço de venda não pode ser negativo.")
    private BigDecimal salePrice = BigDecimal.ZERO;

    @Column(name = "min_quantity", nullable = false)
    @PositiveOrZero(message = "O estoque mínimo não pode ser negativo.")
    private Integer minQuantity = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @NotNull(message = "O produto precisa pertencer a uma categoria.")
    private Category categories;
}

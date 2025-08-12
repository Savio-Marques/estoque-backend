package com.marques.estoque.model;

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
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_debtors")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Debtor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "O nome do devedor não pode ser vazio.")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "value", nullable = false)
    @NotNull(message = "O valor devido não pode ser nulo.")
    @PositiveOrZero(message = "A quantidade não pode ser um número negativo.")
    private BigDecimal value;

    @Column(name = "date")
    private LocalDateTime date;
}

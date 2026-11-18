package com.marques.estoque.model;

import com.marques.estoque.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_debtor_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DebtorTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "debtor_id")
    private Long debtorId;

    @Column(name = "debtor_name", nullable = false)
    private String debtorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private DebtorTransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "previous_balance", nullable = false)
    private BigDecimal previousBalance;

    @Column(name = "new_balance", nullable = false)
    private BigDecimal newBalance;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

package com.marques.estoque.repository;

import com.marques.estoque.model.Debtor;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.projection.DebtorSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DebtorRepository extends JpaRepository<Debtor, Long> {

    List<Debtor> findAllByUser(User user);

    Optional<Debtor> findByIdAndUser(Long id, User user);

    Optional<Debtor> findByNameIgnoreCaseAndUser(String name, User user);

    @Query("SELECT " +
            "COUNT(d.id) AS totalDebtors, " +
            "COALESCE(SUM(d.value), 0) AS totalValue " +
            "FROM Debtor d WHERE d.user = :user")
    DebtorSummaryProjection getDebtorSummary(@Param("user") User user);

    List<Debtor> findByUserAndNameContainingIgnoreCase(User user, String clientName);

}

package com.marques.estoque.repository;

import com.marques.estoque.model.Debtor;
import com.marques.estoque.model.user.User;
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

    List<Debtor> findAllByUserAndPaidFalse(User user);

    List<Debtor> findAllByUserAndPaidTrueOrderBySettledAtDescIdDesc(User user);

    Optional<Debtor> findByIdAndUser(Long id, User user);

    Optional<Debtor> findByNameIgnoreCaseAndUser(String name, User user);

    Optional<Debtor> findFirstByUserAndPaidTrueAndNameIgnoreCaseOrderBySettledAtDescIdDesc(User user, String name);

    boolean existsByIdAndUser(Long id, User user);

    @Query("SELECT SUM(d.value) FROM Debtor d WHERE d.user = :user AND d.paid = false")
    BigDecimal sumTotalValueByUser(@Param("user") User user);

    @Query("SELECT COUNT(d) FROM Debtor d WHERE d.user = :user AND d.paid = false")
    Integer countTotalDebtorsByUser(@Param("user") User user);

    List<Debtor> findByUserAndNameContainingIgnoreCase(User user, String clientName);

    List<Debtor> findByUserAndPaidAndNameContainingIgnoreCase(User user, Boolean paid, String clientName);

    void deleteAllByUser(User user);

}

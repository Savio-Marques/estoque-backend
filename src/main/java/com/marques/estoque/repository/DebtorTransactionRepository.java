package com.marques.estoque.repository;

import com.marques.estoque.model.DebtorTransaction;
import com.marques.estoque.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebtorTransactionRepository extends JpaRepository<DebtorTransaction, Long> {

    List<DebtorTransaction> findAllByDebtorIdAndUserOrderByCreatedAtDescIdDesc(Long debtorId, User user);

    List<DebtorTransaction> findTop50ByUserOrderByCreatedAtDescIdDesc(User user);

    void deleteAllByDebtorIdAndUser(Long debtorId, User user);

    void deleteAllByUser(User user);
}

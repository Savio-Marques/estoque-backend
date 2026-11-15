package com.marques.estoque.repository;

import com.marques.estoque.model.product.StockMovement;
import com.marques.estoque.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findTop50ByUserOrderByCreatedAtDesc(User user);

    void deleteAllByUser(User user);
}

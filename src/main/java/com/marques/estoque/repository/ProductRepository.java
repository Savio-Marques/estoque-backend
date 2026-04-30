package com.marques.estoque.repository;

import com.marques.estoque.model.product.Product;
import com.marques.estoque.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByUserOrderByIdAsc(User user);

    Optional<Product> findByIdAndUser(Long id, User user);

    List<Product> findByNameContainingIgnoreCaseAndUser(String name, User user);

    Optional<Product> findByNameIgnoreCaseAndUser(String name, User user);

    @Query("SELECT " +
            "COUNT(p.id) AS total, " +
            "COALESCE(SUM(CASE WHEN p.qtd > 0 AND p.qtd <= :threshold THEN 1L ELSE 0L END), 0L) AS lowStock, " +
            "COALESCE(SUM(CASE WHEN p.qtd = 0 THEN 1L ELSE 0L END), 0L) AS noStock " +
            "FROM Product p WHERE p.user = :user")
    ProductSummaryProjection getProductSummary(@Param("user") User user, @Param("threshold") int threshold);
}

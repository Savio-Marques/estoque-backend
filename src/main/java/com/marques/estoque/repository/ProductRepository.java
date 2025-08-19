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

    List<Product> findAllByUser(User user);

    Optional<Product> findByIdAndUser(Long id, User user);

    Optional<Product> findByNameIgnoreCaseAndUser(String name, User user);

    boolean existsByNameAndUser(String name, User user);

    boolean existsByIdAndUser(Long id, User user);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.user = :user")
    Integer countTotalProducts(@Param("user") User user);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.user = :user AND p.qtd = 0")
    Integer countOutOfStockProducts(@Param("user") User user);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.user = :user AND p.qtd > 0 AND p.qtd <= :threshold")
    Integer countLowStockProducts(@Param("user") User user, @Param("threshold") int threshold);

    @Query("SELECT p FROM Product p WHERE p.user = :user AND p.name ILIKE CONCAT('%', :name, '%') AND p.categories.name = :categoryName")
    List<Product> findByUserAndNameAndCategory(
            @Param("user") User user,
            @Param("name") String name,
            @Param("categoryName") String categoryName
    );

    // Cenário 2: Busca apenas por NOME
    @Query("SELECT p FROM Product p WHERE p.user = :user AND p.name ILIKE CONCAT('%', :name, '%')")
    List<Product> findByUserAndName(
            @Param("user") User user,
            @Param("name") String name
    );

    // Cenário 3: Busca apenas por CATEGORIA
    @Query("SELECT p FROM Product p WHERE p.user = :user AND p.categories.name = :categoryName")
    List<Product> findByUserAndCategory(
            @Param("user") User user,
            @Param("categoryName") String categoryName
    );

}

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

    @Query("SELECT p FROM Product p JOIN FETCH p.categories WHERE p.user = :user ORDER BY p.id ASC")
    List<Product> findAllByUserOrderByIdAsc(@Param("user") User user);

    @Query("SELECT p FROM Product p JOIN FETCH p.categories WHERE p.id = :id AND p.user = :user")
    Optional<Product> findByIdAndUser(@Param("id") Long id, @Param("user") User user);

    @Query("SELECT p FROM Product p JOIN FETCH p.categories WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.user = :user")
    List<Product> findByNameContainingIgnoreCaseAndUser(@Param("name") String name, @Param("user") User user);

    boolean existsByNameIgnoreCaseAndUser(String name, User user);

    boolean existsByNameIgnoreCaseAndUserAndIdNot(String name, User user, Long id);

    boolean existsByIdAndUser(Long id, User user);

    boolean existsByCategoriesIdAndUser(Long categoryId, User user);

    void deleteAllByUser(User user);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.user = :user")
    Integer countTotalProductsByUser(@Param("user") User user);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.user = :user AND p.qtd = 0")
    Integer countOutOfStockProductsByUser(@Param("user") User user);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.user = :user AND p.qtd > 0 AND p.qtd <= :threshold")
    Integer countLowStockProductsByUser(@Param("user") User user, @Param("threshold") int threshold);

    @Query("SELECT p FROM Product p JOIN FETCH p.categories WHERE p.user = :user AND p.qtd = 0 ORDER BY p.id ASC")
    List<Product> findOutOfStockProductsOrderByIdAsc(@Param("user") User user);

    @Query("SELECT p FROM Product p JOIN FETCH p.categories WHERE p.user = :user AND p.qtd > 0 AND p.qtd <= :threshold ORDER BY p.id ASC")
    List<Product> findLowStockProductsOrderByIdAsc(@Param("user") User user, @Param("threshold") int threshold);
}

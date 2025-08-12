package com.marques.estoque.repository;

import com.marques.estoque.model.product.Product;
import com.marques.estoque.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
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

}

package com.marques.estoque.repository;

import com.marques.estoque.model.product.Category;
import com.marques.estoque.model.product.Product;
import com.marques.estoque.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByUserOrderByIdAsc(User user);

    Optional<Category> findByIdAndUser(Long id, User user);

    Optional<Category> findByNameIgnoreCaseAndUser(String name, User user);

    boolean existsByIdAndUser(Long id, User user);


}

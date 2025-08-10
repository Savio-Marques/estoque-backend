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

    // Busca todos os produtos que pertencem a um usuário específico
    List<Category> findAllByUser(User user);

    // Busca um produto pelo ID, mas SOMENTE se ele pertencer ao usuário fornecido
    Optional<Category> findByIdAndUser(Long id, User user);

    // Busca um produto pelo nome (ignorando maiúsculas/minúsculas), mas SOMENTE se ele pertencer ao usuário
    Optional<Category> findByNameIgnoreCaseAndUser(String name, User user);

    // Verifica se um produto existe pelo nome para um usuário específico
    boolean existsByNameAndUser(String name, User user);

    // Verifica se um produto existe pelo ID para um usuário específico
    boolean existsByIdAndUser(Long id, User user);


}

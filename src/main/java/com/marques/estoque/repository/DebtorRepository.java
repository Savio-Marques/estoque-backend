package com.marques.estoque.repository;

import com.marques.estoque.model.Debtor;
import com.marques.estoque.model.product.Category;
import com.marques.estoque.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DebtorRepository extends JpaRepository<Debtor, Long> {

    // Busca todas as categorias que pertencem a um usuário específico
    List<Debtor> findAllByUser(User user);

    // Busca uma categoria pelo ID, mas SOMENTE se ele pertencer ao usuário fornecido
    Optional<Debtor> findByIdAndUser(Long id, User user);

    // Busca uma categoria pelo nome (ignorando maiúsculas/minúsculas), mas SOMENTE se ele pertencer ao usuário
    Optional<Debtor> findByNameIgnoreCaseAndUser(String name, User user);

    // Verifica se umuma categoria existe pelo nome para um usuário específico
    boolean existsByNameAndUser(String name, User user);

    // Verifica se uma categoria existe pelo ID para um usuário específico
    boolean existsByIdAndUser(Long id, User user);
}

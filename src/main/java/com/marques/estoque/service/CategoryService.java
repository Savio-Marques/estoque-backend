package com.marques.estoque.service;

import com.marques.estoque.dto.CategoryDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.exception.GeneralException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.product.Category;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.CategoryRepository;
import com.marques.estoque.util.CategoryMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryDTO findById(Long id) {
        log.info("Procurado por ID");
        return CategoryMapper.INSTANCE.toDTO(returnCategory(id, getCurrentUser()));
    }

    public CategoryDTO findByName(String name) {
        log.info("Procurando por nome");

        return CategoryMapper.INSTANCE.toDTO(returnProductWithName(name, getCurrentUser()));
    }

    public List<CategoryDTO> findAll() {
        log.info("Buscando todos");

        List<Category> categories = categoryRepository.findAllByUserOrderByIdAsc(getCurrentUser());

        if (categories.isEmpty()) {
            log.error("Nenhuma categoria encontrada");
            throw new NotFoundException("Nenhuma categoria encontrada");
        }

        return CategoryMapper.INSTANCE.toDTOList(categories);
    }

    public CategoryDTO save(CategoryDTO categoryDTO) {
        log.info("Cadastrando categoria");

        validateAndCheckCategoryName(categoryDTO.getName());

        Category category = CategoryMapper.INSTANCE.toEntity(categoryDTO);

        category.setUser(getCurrentUser());

        return CategoryMapper.INSTANCE.toDTO(categoryRepository.save(category));
    }

    public CategoryDTO update (Long id, CategoryDTO categoryDTO) {
        log.info("Atualizando a categoria: {}", id);
        Category category = returnCategory(id, getCurrentUser());

        validateAndCheckCategoryName(categoryDTO.getName());

        category.setName(categoryDTO.getName());
        return CategoryMapper.INSTANCE.toDTO(categoryRepository.save(category));
    }

    public String delete(Long id) {
        log.info("Deletando a categoria: {}", id);
        if (!categoryRepository.existsByIdAndUser(id, getCurrentUser())) {
            log.error("Categoria {} não encontrada", id);
            throw new NotFoundException("Categoria " + id + " não encontrada");
        }

        if (!findById(id).getProducts().isEmpty()) {
            log.error("Categoria com um ou mais produtos registados");
            throw new GeneralException("Não é possível excluir categorias com produtos associodados, exluir produtos primeiro.");
        }

        categoryRepository.deleteById(id);
        return "Categoria "  + id + " excluida com sucesso";
    }

    /*

    -------------FUNÇÕES AUXILIARES-------------

    */

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Category returnCategory(Long id, User user) {
        return categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Categoria com de id" + id + " não encontrada"));
    }

    private Category returnProductWithName(String name, User user) {
        return categoryRepository.findByNameIgnoreCaseAndUser(name, user)
                .orElseThrow(() -> new NotFoundException("Categoria "+ name + " não encontrada"));
    }


    private void validateAndCheckCategoryName(String name){
        if (name == null || name.isEmpty()) {
            log.error("Nome da categoria não pode ser vazio");
            throw new ArgumentException("Nome da categoria não pode ser vazio");
        }

        if (categoryRepository.findByNameIgnoreCaseAndUser(name, getCurrentUser()).isPresent()) {
            log.error("Categoria já existente");
            throw new DuplicateDataException("Categoria " + name + " já existente");
        }
    }
}


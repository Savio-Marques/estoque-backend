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
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        log.info("Procurado por ID");
        return categoryMapper.toDTO(returnCategory(id, getCurrentUser()));
    }

    @Transactional(readOnly = true)
    public CategoryDTO findByName(String name) {
        log.info("Procurando por nome");

        return categoryMapper.toDTO(returnCategoryByName(name, getCurrentUser()));
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll() {
        log.info("Buscando todos");

        List<Category> categories = categoryRepository.findAllByUserOrderByIdAsc(getCurrentUser());

        return categoryMapper.toDTOList(categories);
    }

    @Transactional
    public CategoryDTO save(CategoryDTO categoryDTO) {
        log.info("Cadastrando categoria");
        String name = normalizeName(categoryDTO.getName());
        validateNameForCreate(name);

        Category category = categoryMapper.toEntity(categoryDTO);
        category.setName(name);
        category.setUser(getCurrentUser());

        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO update (Long id, CategoryDTO categoryDTO) {
        log.info("Atualizando a categoria: {}", id);
        Category category = returnCategory(id, getCurrentUser());
        String name = normalizeName(categoryDTO.getName());
        validateNameForUpdate(id, name);

        category.setName(name);
        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deletando a categoria: {}", id);
        Category category = returnCategory(id, getCurrentUser());

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            log.error("Categoria com um ou mais produtos registrados");
            throw new GeneralException("Não é possível excluir categorias com produtos associados. Exclua os produtos primeiro.");
        }

        categoryRepository.deleteById(id);
    }

    /*

    -------------FUNÇÕES AUXILIARES-------------

    */

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Category returnCategory(Long id, User user) {
        return categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Categoria com id " + id + " não encontrada"));
    }

    private Category returnCategoryByName(String name, User user) {
        return categoryRepository.findByNameIgnoreCaseAndUser(name, user)
                .orElseThrow(() -> new NotFoundException("Categoria "+ name + " não encontrada"));
    }
    private String normalizeName(String name) {
        if (name == null) return null;
        return name.trim();
    }

    private void validateNameNotBlank(String name) {
        if (name == null || name.isBlank()) {
            log.error("Nome da categoria não pode ser vazio");
            throw new ArgumentException("Nome da categoria não pode ser vazio");
        }
    }

    private void validateNameForCreate(String name) {
        validateNameNotBlank(name);
        if (categoryRepository.findByNameIgnoreCaseAndUser(name, getCurrentUser()).isPresent()) {
            log.error("Categoria já existente");
            throw new DuplicateDataException("Categoria " + name + " já existente");
        }
    }

    private void validateNameForUpdate(Long id, String name) {
        validateNameNotBlank(name);
        var existing = categoryRepository.findByNameIgnoreCaseAndUser(name, getCurrentUser());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            log.error("Categoria já existente para outro id");
            throw new DuplicateDataException("Categoria " + name + " já existente");
        }
    }
}


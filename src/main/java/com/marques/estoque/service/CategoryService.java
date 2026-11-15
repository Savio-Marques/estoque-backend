package com.marques.estoque.service;

import com.marques.estoque.dto.CategoryDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.exception.GeneralException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.product.Category;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.CategoryRepository;
import com.marques.estoque.repository.ProductRepository;
import com.marques.estoque.security.AuthenticatedUserProvider;
import com.marques.estoque.util.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public CategoryDTO findById(Long id) {
        log.info("Buscando categoria id: {} para o usuário logado", id);
        return categoryMapper.toDTO(returnCategory(id, authenticatedUserProvider.getCurrentUser()));
    }

    public CategoryDTO findByName(String name) {
        log.info("Buscando categoria por nome: {} para o usuário logado", name);
        return categoryMapper.toDTO(returnCategoryWithName(name, authenticatedUserProvider.getCurrentUser()));
    }

    public List<CategoryDTO> findAll() {
        log.info("Buscando todas as categorias para o usuário logado");
        List<Category> categories = categoryRepository.findAllByUserOrderByIdAsc(authenticatedUserProvider.getCurrentUser());
        return categoryMapper.toDTOList(categories);
    }

    public CategoryDTO save(CategoryDTO categoryDTO) {
        log.info("Cadastrando nova categoria: {} para o usuário logado", categoryDTO.getName());
        User currentUser = authenticatedUserProvider.getCurrentUser();

        validateAndCheckCategoryName(categoryDTO.getName(), currentUser, null);

        Category category = categoryMapper.toEntity(categoryDTO);
        category.setUser(currentUser);

        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    public CategoryDTO update(Long id, CategoryDTO categoryDTO) {
        log.info("Atualizando a categoria id: {} para o usuário logado", id);
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Category category = returnCategory(id, currentUser);

        validateAndCheckCategoryName(categoryDTO.getName(), currentUser, id);

        category.setName(categoryDTO.getName());
        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    public String delete(Long id) {
        log.info("Deletando a categoria: {}", id);
        User currentUser = authenticatedUserProvider.getCurrentUser();

        if (!categoryRepository.existsByIdAndUser(id, currentUser)) {
            log.error("Categoria {} não encontrada", id);
            throw new NotFoundException("Categoria " + id + " não encontrada");
        }

        if (productRepository.existsByCategoriesIdAndUser(id, currentUser)) {
            log.error("Categoria com um ou mais produtos registrados");
            throw new GeneralException("Não é possível excluir categorias com produtos associados, excluir produtos primeiro.");
        }

        categoryRepository.deleteById(id);
        return "Categoria " + id + " excluida com sucesso";
    }

    public Category returnCategory(Long id, User user) {
        return categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Categoria com o id " + id + " não encontrada"));
    }

    private Category returnCategoryWithName(String name, User user) {
        return categoryRepository.findByNameIgnoreCaseAndUser(name, user)
                .orElseThrow(() -> new NotFoundException("Categoria " + name + " não encontrada"));
    }

    private void validateAndCheckCategoryName(String name, User currentUser, Long excludeId) {
        if (name == null || name.trim().isEmpty()) {
            log.error("Nome da categoria não pode ser vazio");
            throw new ArgumentException("Nome da categoria não pode ser vazio");
        }

        boolean exists = (excludeId == null)
                ? categoryRepository.findByNameIgnoreCaseAndUser(name, currentUser).isPresent()
                : categoryRepository.existsByNameIgnoreCaseAndUserAndIdNot(name, currentUser, excludeId);

        if (exists) {
            log.error("Categoria já existente");
            throw new DuplicateDataException("Categoria " + name + " já existente");
        }
    }
}

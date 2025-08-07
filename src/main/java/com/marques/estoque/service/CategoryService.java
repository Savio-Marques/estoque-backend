package com.marques.estoque.service;

import com.marques.estoque.dto.CategoryDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.exception.GeneralException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.Category;
import com.marques.estoque.repository.CategoryRepository;
import com.marques.estoque.util.CategoryMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryDTO findById(Long id) {
        log.info("Procurado por ID");
        return CategoryMapper.INSTANCE.toDTO(returnCategory(id));
    }

    public CategoryDTO findByName(String name) {
        log.info("Procurando por nome");

        return CategoryMapper.INSTANCE.toDTO(returnProductWithName(name));
    }

    public List<CategoryDTO> findAll() {
        log.info("Buscando todos");

        List<Category> categories = categoryRepository.findAll();

        if (categories.isEmpty()) {
            log.error("Nenhuma categoria encontrada");
            throw new NotFoundException("Nenhuma categoria encontrada");
        }

        return CategoryMapper.INSTANCE.toDTOList(categories);
    }

    public CategoryDTO save(CategoryDTO categoryDTO) {
        log.info("Cadastrando categoria");

        validateAndCheckCategoryName(categoryDTO.getName());

        return CategoryMapper.INSTANCE.toDTO(categoryRepository.save(CategoryMapper.INSTANCE.toEntity(categoryDTO)));
    }

    public CategoryDTO update (Long id, CategoryDTO categoryDTO) {
        log.info("Atualizando a categoria: {}", id);
        Category category = returnCategory(id);

        validateAndCheckCategoryName(categoryDTO.getName());

        category.setName(categoryDTO.getName());
        return CategoryMapper.INSTANCE.toDTO(categoryRepository.save(category));
    }

    public String delete(Long id) {
        log.info("Deletando a categoria: {}", id);
        if (!categoryRepository.existsById(id)) {
            log.error("Categoria {} não encontrada", id);
            throw new NotFoundException("Categoria " + id + " não encontrada");
        }

        if (!findById(id).getProducts().isEmpty()) {
            log.error("Categoria com um ou mais produtos registados");
            throw new GeneralException("Não é possível excluir categorias com um ou mais produtos registados");
        }

        categoryRepository.deleteById(id);
        return "Categoria "  + id + " excluida com sucesso";
    }

    /*

    -------------FUNÇÕES AUXILIARES-------------

    */

    public Category returnCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoria com de id" + id + " não encontrada"));
    }

    private Category returnProductWithName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Categoria "+ name + " não encontrada"));
    }


    private void validateAndCheckCategoryName(String name){
        if (name == null || name.isEmpty()) {
            log.error("Nome da categoria não pode ser vazio");
            throw new ArgumentException("Nome da categoria não pode ser vazio");
        }

        if (categoryRepository.existsByName(name)) {
            log.error("Categoria já existente");
            throw new DuplicateDataException("Categoria " + name + " já existente");
        }
    }
}


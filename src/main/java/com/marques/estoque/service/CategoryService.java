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
        log.info("Searching by id");
        return CategoryMapper.INSTANCE.toDTO(returnCategory(id));
    }

    public List<CategoryDTO> findAll() {
        log.info("Searching All");

        List<Category> categories = categoryRepository.findAll();

        if (categories.isEmpty()) {
            log.error("No categories found in database");
            throw new NotFoundException("No categories found in database");
        }

        return CategoryMapper.INSTANCE.toDTOList(categories);
    }

    public CategoryDTO save(CategoryDTO categoryDTO) {
        log.info("Saving category in database");

        validateAndCheckCategoryName(categoryDTO.getName());

        return CategoryMapper.INSTANCE.toDTO(categoryRepository.save(CategoryMapper.INSTANCE.toEntity(categoryDTO)));
    }

    public CategoryDTO update (Long id, CategoryDTO categoryDTO) {
        log.info("Updating id: {} category in database", id);
        Category category = returnCategory(id);

        validateAndCheckCategoryName(categoryDTO.getName());

        category.setName(categoryDTO.getName());
        return CategoryMapper.INSTANCE.toDTO(categoryRepository.save(category));
    }

    public String delete(Long id) {
        log.info("Deleting id: {} category in database", id);
        if (!categoryRepository.existsById(id)) {
            log.error("Category not found with id: {}", id);
            throw new NotFoundException("Category not found with id: " + id);
        }

        if (!findById(id).getProducts().isEmpty()) {
            log.error("Category with one our more registred products");
            throw new GeneralException("Category with one our more registred products");
        }

        categoryRepository.deleteById(id);
        return "Category "  + id + " deleted with successfully";
    }

    public Category returnCategory(Long id) {

        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
    }

    private void validateAndCheckCategoryName(String name){
        if (name == null || name.isEmpty()) {
            log.error("Category name cannot be a null our empty");
            throw new ArgumentException("Category name cannot be a null our empty");
        }

        if (categoryRepository.existsByName(name)) {
            log.error("Category already exists");
            throw new DuplicateDataException("Category " + name + " already exists");
        }
    }
}


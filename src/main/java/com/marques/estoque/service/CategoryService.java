package com.marques.estoque.service;

import com.marques.estoque.dto.CategoryDTO;
import com.marques.estoque.model.Category;
import com.marques.estoque.repository.CategoryRepository;
import com.marques.estoque.util.CategoryMapper;
import com.marques.estoque.util.ProductMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryDTO findById(Long id) {
        return CategoryMapper.INSTANCE.toDTO(returnCategory(id));
    }

    public List<CategoryDTO> findAll() {
        return CategoryMapper.INSTANCE.toDTOList(categoryRepository.findAll());
    }

    public CategoryDTO save(CategoryDTO categoryDTO) {
        return CategoryMapper.INSTANCE.toDTO(categoryRepository.save(CategoryMapper.INSTANCE.toEntity(categoryDTO)));
    }

    public CategoryDTO update (Long id, CategoryDTO categoryDTO) {
        Category category = returnCategory(id);
        category.setName(categoryDTO.getName());
        return CategoryMapper.INSTANCE.toDTO(categoryRepository.save(category));
    }

    public String delete(Long id) {
        categoryRepository.deleteById(id);
        return "Category" + id + "deleted";
    }
    private Category returnCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
    }
}


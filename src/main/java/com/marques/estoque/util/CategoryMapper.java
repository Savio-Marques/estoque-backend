package com.marques.estoque.util;

import com.marques.estoque.dto.CategoryDTO;
import com.marques.estoque.model.product.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "products", ignore = true)
    CategoryDTO toDTO(Category category);

    @Mapping(target = "products", ignore = true)
    List<CategoryDTO> toDTOList(List<Category> categories);

    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryDTO dto);
}
package com.marques.estoque.util;

import com.marques.estoque.dto.CategoryDTO;
import com.marques.estoque.model.product.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    // Evita N+1 queries e LazyInitializationException na listagem de categorias
    @Mapping(target = "products", ignore = true)
    CategoryDTO toDTO(Category category);

    List<CategoryDTO> toDTOList(List<Category> categories);

    @Mapping(target = "products", ignore = true)
    @Mapping(target = "user", ignore = true)
    Category toEntity(CategoryDTO dto);
}

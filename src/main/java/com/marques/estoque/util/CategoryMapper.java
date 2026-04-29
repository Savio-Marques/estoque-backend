package com.marques.estoque.util;

import com.marques.estoque.dto.CategoryDTO;
import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.model.product.Category;
import com.marques.estoque.model.product.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    @Mapping(target = "products", ignore = true)
    CategoryDTO toDTO(Category category);

    @Mapping(target = "products", ignore = true)
    List<CategoryDTO> toDTOList(List<Category> categories);

    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryDTO dto);
}
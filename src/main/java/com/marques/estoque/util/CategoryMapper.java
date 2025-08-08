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

    @Mapping(target = "products", expression = "java(mapProducts(category.getProducts()))")
    CategoryDTO toDTO(Category category);

    @Mapping(target = "products", expression = "java(mapProducts(category.getProducts()))")
    List<CategoryDTO> toDTOList(List<Category> categories);

    @Mapping(target = "products", ignore = true) // Os produtos serão definidos no service
    Category toEntity(CategoryDTO dto);

    default List<ProductDTO> mapProducts(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream()
                .map( product -> {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setId(product.getId());
                    productDTO.setName(product.getName());
                    productDTO.setQtd(product.getQtd());
                    productDTO.setCategoryId(product.getCategories().getId());
                    return productDTO;
                })
                .collect(Collectors.toList());
    }
}
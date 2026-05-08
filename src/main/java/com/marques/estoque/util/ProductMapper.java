package com.marques.estoque.util;

import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.model.product.Product;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "categories.id", target = "categoryId")
    @Mapping(source = "categories.name", target = "categoryName")
    ProductDTO toDTO(Product product);

    List<ProductDTO> toDTOList(List<Product> products);

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    Product toEntity(ProductDTO productDTO);

}
package com.marques.estoque.util;

import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.model.product.Product;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "categories.id", target = "categoryId")
    @Mapping(source = "categories.name", target = "categoryName")
    ProductDTO toDTO(Product product);

    List<ProductDTO> toDTOList(List<Product> products);

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "user", ignore = true)
    Product toEntity(ProductDTO productDTO);

    @AfterMapping
    default void calculateStatus(@MappingTarget ProductDTO dto, Product product) {
        if (product == null) {
            return;
        }

        if (product.getQtd() == 0) {
            dto.setStatus("Sem Estoque");
        } else if (product.getQtd() <= 5) {
            dto.setStatus("Estoque Baixo");
        } else {
            dto.setStatus("Disponível");
        }
    }
}